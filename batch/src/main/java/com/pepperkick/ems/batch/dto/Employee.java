package com.pepperkick.ems.batch.dto;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

public class Employee {
    private int id;
    private String name;
    private int designationId;
    private int managerId;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getDesignationId() {
        return designationId;
    }

    public void setDesignationId(int designationId) {
        this.designationId = designationId;
    }

    public int getManagerId() {
        return managerId;
    }

    public void setManagerId(int managerId) {
        this.managerId = managerId;
    }

    public String toString() {
        try {
            JSONObject json = new JSONObject();
            json.put("id", id);
            json.put("name", name);
            json.put("designationId", designationId);
            json.put("managerId", managerId);
            return json.toString();
        } catch (JSONException e) {
            return "JSON Error processing Employee";
        } catch (Exception e) {
            return "Unknown Error processing Employee";
        }
    }
}
