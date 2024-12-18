package com.jobhunter.Cleaner.interfaces;

import org.json.JSONObject;

public interface JobCleaner {
    JSONObject cleanJobOffer(JSONObject rawJobOffer);
}
