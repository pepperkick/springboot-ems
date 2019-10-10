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
result_passes=0
result_fails=0
RED='\033[0;31m'
GREEN='\033[0;32m'
NC='\033[0m'

# Helper functions
function printTestCase() {
  if [ "$1" == "true" ]; then
    echo -e "${GREEN}[o]${NC} Test Case $test_num: $description"
    result_passes=$((result_passes+1))
  else
    echo -e "${RED}[x]${NC} Test Case $test_num: $description"
    result_fails=$((result_fails+1))
  fi
}

function incrementTestNum() {
  test_num=$((test_num+1))
}

# GET all employees list
incrementTestNum
description="Perform GET all employees operation"

response=$(curl --request GET -s \
     --url "$apiUrl/employees")
listSize=$(echo "$response" | $jq ". | length")

if [ "$listSize" == "10" ]; then
  printTestCase true
else
  printTestCase false
  echo "Response list size should be 10 but found $listSize"
  echo ""
fi

# GET employee by ID
incrementTestNum
description="Perform GET employee by ID operation"

response=$(curl --request GET -s \
     --url "$apiUrl/employees/2")

id=$(echo "$response" | $jq ".id")
title=$(echo "$response" | $jq ".name")
jobTitle=$(echo "$response" | $jq ".jobTitle")
manager=$(echo "$response" | $jq ".manager.id")
suboridnatesSize=$(echo "$response" | $jq ".subordinates | length")
colleaguesSize=$(echo "$response" | $jq ".colleagues | length")

if [ "$id" == "2" ] && [ "$title" == "\"Iron Man\"" ] && [ "$jobTitle" == "\"Manager\"" ] && [ "$manager" == "1" ]; then
  printTestCase true
else
  printTestCase false
  if [ "$id" != "2" ]; then
    echo "Response should have ID 2 but found $id"
  fi
  if [ "$title" != "\"Iron Man\"" ]; then
    echo "Response should have name \"Iron Man\" but found $title"
  fi
  if [ "$jobTitle" != "\"Manager\"" ]; then
    echo "Response should have job title \"Manager\" but found $jobTitle"
  fi
  if [ "$manager" != "1" ]; then
    echo "Response should have manager ID 1 but found $manager"
  fi
  echo ""
fi

# POST new employee
incrementTestNum
description="Perform POST and add new employee operation"

id=$(curl --request POST -s \
      --header "Content-Type: application/json" \
      --data '{"name": "Black Panther", "jobTitle": "Manager", "managerId": 1 }' \
      --url "$apiUrl/employees" | $jq ".id")

title=$(curl --request GET -s \
      --url "$apiUrl/employees/$id" | $jq ".name")

if [ "$title" == "\"Black Panther\"" ]; then
  printTestCase true
else
  printTestCase false
  echo "Response name should be \"Black Panther\" but found $title"
  echo ""
fi

# DELETE employee by ID
incrementTestNum
description="Perform DELETE employee by ID operation"

status1=$(curl --request DELETE -sw "RESP_CODE:%{response_code}" \
      --url "$apiUrl/employees/2" | grep -o 'RESP_CODE:[1-4][0-9][0-9]')

status2=$(curl --request GET -sw "RESP_CODE:%{response_code}" \
      --url "$apiUrl/employees/2" | grep -o 'RESP_CODE:[1-4][0-9][0-9]')

if [ "$status1" == "RESP_CODE:200" ] && [ "$status2" == "RESP_CODE:404" ]; then
  printTestCase true
else
  printTestCase false
  if [ "$status1" != "RESP_CODE:200" ]; then
    echo "DELETE response status should be 200 but found $status1"
  fi
  if [ "$status2" != "RESP_CODE:404" ]; then
    echo "GET response status should be 404 but found $status2"
  fi
  echo ""
fi

# Check if deleted employee's subordinate's manager changed
incrementTestNum
description="Check if deleted employee's subordinates' manager ID was changed"

managerId1=$(curl --request GET -s \
      --url "$apiUrl/employees/5" | $jq ".manager.id")

managerId2=$(curl --request GET -s \
      --url "$apiUrl/employees/6" | $jq ".manager.id")

if [ "$managerId1" == "1" ] && [ "$managerId2" == "1" ]; then
  printTestCase true
else
  printTestCase false
  echo "Manager ID of subordinates of deleted employee was not updated, required 1 but found $managerId1"
  echo ""
fi

# Results
echo ""
echo "------------------------------"
echo "Passed: ($result_passes / $test_num) | Failed: ($result_fails / $test_num)"
echo "------------------------------"
