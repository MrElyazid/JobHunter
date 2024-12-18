package com.jobhunter.Cleaner;

import org.json.JSONObject;

public interface JobCleaner {
    JSONObject cleanJobOffer(JSONObject rawJobOffer);
}
