/*
 * Copyright (c) 2020-2020 Splunk, Inc. All rights reserved.
 */

package com.splunk.streaming.user.functions;

import com.splunk.streaming.data.Record;
import com.splunk.streaming.flink.test.TestRunner;
import com.splunk.streaming.flink.test.TestSource;
import com.splunk.streaming.upl3.core.ApiVersion;
import com.splunk.streaming.upl3.language.UplSchemaToRecordDescriptor;
import com.splunk.streaming.upl3.language.VersionedFunctionRegistry;
import com.splunk.streaming.upl3.type.SchemaType;
import com.splunk.streaming.upl3.type.StringType;

import com.google.common.collect.Lists;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

public class TestVariableWriteLogFunctionIT {

  private TestRunner runner;
  private SchemaType inputSchema;

  @Before
  public void setUp() throws Exception {
    // Create a new TestRunner to execute the integration tests
    VersionedFunctionRegistry registry = new VersionedFunctionRegistry(ApiVersion.V3_BETA_1);
    runner = new TestRunner(registry, new VariableWriteLogFunction());

    // Shared input schema for running tests.  Add more schemas as needed
    inputSchema = SchemaType.newBuilder()
      .addField("body", StringType.INSTANCE)
      .addField("level", StringType.INSTANCE)
      .build();
  }

  @Test
  // test function using the TestRunner.  Add more tests as needed for test coverage
  public void testVariableWriteLogFunction() throws Exception {
    // Create and register test data stream source to test your function
    TestSource.registerSource("input", generateInputRecords(), inputSchema);

    // SPL for your test
    String spl = "| from test_source(\"input\") | into variable_write_log(logger_name: \"test-logger\", level: level);";

    // compile the spl, run the pipeline, and validate results against an expected records list.
    runner.runSynchronousTestWithSink(spl);
  }

  // Input records for a test.  Add more generators as needed for test coverage
  // Records are processed in order
  private List<Record> generateInputRecords() {
    List<Record> records = Lists.newArrayListWithCapacity(10);
    for (int idx = 0; idx < 10; idx++) {
      // create new record with the given schema
      Record record = new Record(UplSchemaToRecordDescriptor.convert(inputSchema));

      // add fields to the record
      record.put("body", "Record " + idx);
      record.put("level", (idx % 2 == 0 ? "error" : "warn"));

      // add record to the return list
      records.add(record);
    }
    return records;
  }
}
