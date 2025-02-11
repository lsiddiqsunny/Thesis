package demos.dlineage.sqlenv.sqldep;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import gudusoft.gsqlparser.EDbVendor;
import gudusoft.gsqlparser.sqlenv.TSQLEnv;
import gudusoft.gsqlparser.sqlenv.TSQLSchema;
import gudusoft.gsqlparser.sqlenv.TSQLTable;

public class SQLDepSQLEnv extends TSQLEnv {

	private JSONObject jsonContent;

	private Boolean init = false;

	public SQLDepSQLEnv(EDbVendor dbVendor, JSONObject jsonContent) {
		super(dbVendor);
		this.jsonContent = jsonContent;
		initSQLEnv();
	}

	@Override
	public void initSQLEnv() {
		synchronized (init) {
			if (jsonContent == null || init)
				return;

			JSONObject databaseModel = jsonContent.getJSONObject("databaseModel");
			if (databaseModel != null) {
				JSONArray databases = databaseModel.getJSONArray("databases");
				if (databases != null) {
					for (int i = 0; i < databases.size(); i++) {
						JSONObject jsonDatabase = databases.getJSONObject(i);
						String databaseName = jsonDatabase.getString("name");
						JSONArray tables = jsonDatabase.getJSONArray("tables");
						for (int j = 0; j < tables.size(); j++) {
							JSONObject jsonTable = tables.getJSONObject(j);
							String schemeName = jsonTable.getString("schema");
							String tableName = jsonTable.getString("name");
							TSQLSchema sqlSchema = getSQLSchema(databaseName + "." + schemeName, true);
							TSQLTable sqlTable = sqlSchema.createTable(tableName);
							if (jsonTable.containsKey("isView")) {
								sqlTable.setView(Boolean.parseBoolean(jsonTable.getString("isView")));
							}
							JSONArray columns = jsonTable.getJSONArray("columns");
							for (int k = 0; k < columns.size(); k++) {
								JSONObject jsonColumn = columns.getJSONObject(k);
								sqlTable.addColumn(jsonColumn.getString("name"));
							}
						}
					}
				}
				jsonContent = null;
				init = true;
			}
		}
	}

}
