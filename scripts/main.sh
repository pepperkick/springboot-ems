#!/bin/bash

clear

echo "Starting EMS Shell Script"
echo ""

# Variables
serverHost=${SERVER_HOST:="localhost"}
serverPort=${SERVER_PORT:=8080}
apiPrefix=${SERVER_API_PREFIX:="api/v1"}
apiUrl="http://$serverHost:$serverPort/$apiPrefix"
jq=/jq-win64.exe
test_num=0
test_flag=()
result_passes=0
result_fails=0
RED='\033[0;31m'
GREEN='\033[0;32m'
NC='\033[0m'

# Helper functions
function newTestCase() {
  test_num=$((test_num+1))
  description=$1
}

function printTestCase() {
  if [ "$1" == "true" ]; then
    echo -e "${GREEN}[o]${NC} Test Case $test_num: $description"
    result_passes=$((result_passes+1))
    test_flag[$test_num]="1"
  else
    echo -e "${RED}[x]${NC} Test Case $test_num: $description"
    result_fails=$((result_fails+1))
    test_flag[$test_num]="0"
  fi
}

function printResults() {
  echo ""
  echo "------------------------------"
  echo "Passed: ($result_passes / $test_num) | Failed: ($result_fails / $test_num)"
  echo "------------------------------"
}

function formatResponse() {
  status=$(echo "$response" | grep "RESP_CODE:")
  body="${response%??????????????}"
  statusCode="${status:10}"
}

function getOperation() {
  response=$(curl --request GET -sw "\nRESP_CODE:%{response_code}" \
      --url "$apiUrl/employees")
   formatResponse
}

function postOperation() {
  response=$(curl --request POST -sw "\nRESP_CODE:%{response_code}" \
      --header "Content-Type: application/json" \
      --data "$1" \
      --url "$apiUrl/employees")
   formatResponse
}

function getByIdOperation() {
  response=$(curl --request GET -sw "\nRESP_CODE:%{response_code}" \
      --url "$apiUrl/employees/$1")
   formatResponse
}

function deleteByIdOperation() {
  response=$(curl --request DELETE -sw "\nRESP_CODE:%{response_code}" \
      --url "$apiUrl/employees/$1")
   formatResponse
}

names=( "Thor" "Iron Man" "Hulk" "Captain America" "War Machine" "Vision" "Falcon" "Ant Man" "Spider Man" "Black Widow" )
jobs=( "Director" "Manager" "Lead" "Manager" "QA" "DevOps" "Developer" "Lead" "Intern" "Developer" )
managers=( -1 1 1 1 2 2 4 4 2 3 )
function generateData() {
    for i in {0..9}
    do
      postOperation  "{ \"name\": \"${names[i]}\", \"jobTitle\": \"${jobs[i]}\", \"managerId\": ${managers[i]} }"
      if [ "$statusCode" != "201" ]; then
        printTestCase false
        echo "Faild to add initial data, response status code should have been \"201\" but found \"$statusCode\""
        echo "Response body: $body"
        echo ""
        printResults
        exit
      fi
    done
}

# TEST 1
# Initialize dummy data
newTestCase "Initialize with dummy data"
generateData
printTestCase true

# TEST 2
# GET all employees list
newTestCase "Perform GET all employees operation"
getOperation

listSize=$(echo "$body" | $jq ". | length")

if [ "$listSize" == "10" ]; then
  printTestCase true
else
  printTestCase false
  echo "Response list size should be 10 but found $listSize"
  echo ""
fi

# TEST 3
# Check if employee list is sorted
newTestCase "Check if employees list is soreted"
sortedArray=(1 4 2 8 3 10 7 6 5 9)
failedFlag=0
for i in {0..9}
do
  id=$(echo "$body" | $jq ".[$i].id")
  if [ "$id" != "${sortedArray[i]}" ]; then
    printTestCase false
    echo "List of employees was not printed in sorted order"
    failedFlag=1
    break
  fi
done

if [ "$failedFlag" == "0" ]; then
  printTestCase true
fi

# TEST 4
# GET employee by ID
newTestCase "Perform GET employee by ID operation"
getByIdOperation 2

id=$(echo "$body" | $jq ".id")
title=$(echo "$body" | $jq ".name")
jobTitle=$(echo "$body" | $jq ".jobTitle")
nestedId=$(echo "$body" | $jq ".employee.id")
nestedTitle=$(echo "$body" | $jq ".employee.name")
nestedJobTitle=$(echo "$body" | $jq ".employee.jobTitle")
manager=$(echo "$body" | $jq ".manager.id")
suboridnatesSize=$(echo "$body" | $jq ".subordinates | length")
colleaguesSize=$(echo "$body" | $jq ".colleagues | length")

if [ "$id" == "2" ] || [ "$nestedId" == "2" ]; then
  flagId=1
fi
if [ "$title" == "\"Iron Man\"" ] || [ "$nestedTitle" == "\"Iron Man\"" ]; then
  flagTitle=1
fi
if [ "$jobTitle" == "\"Manager\"" ] || [ "$nestedJobTitle" == "\"Manager\"" ]; then
  flagJobTitle=1
fi

if [ "$flagId" == "1" ] && [ "$flagTitle" == "1" ] && [ "$flagJobTitle" == "1" ] && [ "$manager" == "1" ] && [ "$suboridnatesSize" == "3" ] && [ "$colleaguesSize" == "2" ]; then
  printTestCase true
else
  printTestCase false
  if [ "$id" != "2" ]; then
    echo "Response should have ID \"2\" but found \"$id\""
  fi
  if [ "$title" != "\"Iron Man\"" ]; then
    echo "Response should have name \"Iron Man\" but found \"$title\""
  fi
  if [ "$jobTitle" != "\"Manager\"" ]; then
    echo "Response should have job title \"Manager\" but found \"$jobTitle\""
  fi
  if [ "$manager" != "1" ]; then
    echo "Response should have manager ID \"1\" but found \"$manager\""
  fi
  if [ "$suboridnatesSize" != "3" ]; then
    echo "Response should have suboridnates array of size \"3\" but found \"$suboridnatesSize\""
  fi
  if [ "$colleaguesSize" != "2" ]; then
    echo "Response should have colleagues array of size \"2\" but found \"$colleaguesSize\""
  fi
  echo "Response body: $body"
  echo ""
fi

# TEST 5
# POST new employee
newTestCase "Perform POST and add new employee operation"

postOperation '{ "name": "Black Panther", "jobTitle": "Manager", "managerId": 1 }'
id=$(echo "$body"| $jq ".id")

getByIdOperation "$id"
title=$(echo "$body" | $jq ".name")

if [ "$title" == "\"Black Panther\"" ]; then
  printTestCase true
else
  printTestCase false
  echo "Response name should be \"Black Panther\" but found $title"
  echo "Response body: $body"
  echo ""
fi

# TEST 6
# DELETE employee by ID
newTestCase "Perform DELETE employee by ID operation"
deleteByIdOperation 2
status1="$statusCode"
body1="$body"

getByIdOperation 2
status2="$statusCode"

if [ "$status1" == "200" ] && [ "$status2" == "404" ]; then
  printTestCase true
else
  printTestCase false
  if [ "$status1" != "200" ]; then
    echo "DELETE response status should be \"200\" but found \"$status1\""
    echo "Response body: $body1"
  fi
  if [ "$status2" != "404" ]; then
    echo "GET response status should be \"404\" but found \"$status2\""
  fi
  echo ""
fi

# TEST 7
# Check if deleted employee's subordinate's manager changed
newTestCase "Check if deleted employee's subordinates' manager ID was changed"

if [ "${test_flag[$test_num-1]}" == "0" ]; then
  printTestCase false
  echo "Cannot run this test as test $((test_num-1)) has failed"
  echo ""
else
  getByIdOperation 5
  managerId1=$(echo "$body"| $jq ".manager.id")

  getByIdOperation 6
  managerId2=$(echo "$body"| $jq ".manager.id")

  if [ "$managerId1" == "1" ] && [ "$managerId2" == "1" ]; then
    printTestCase true
  else
    printTestCase false
    echo "Manager ID of subordinates of deleted employee was not updated, required 1 but found $managerId1"
    echo ""
  fi
fi

printResults
