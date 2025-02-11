package test.postgresql;
/*
 * Date: 13-11-29
 */

import gudusoft.gsqlparser.*;
import gudusoft.gsqlparser.nodes.*;
import gudusoft.gsqlparser.stmt.TSelectSqlStatement;
import gudusoft.gsqlparser.stmt.postgresql.TPostgresqlCreateFunction;
import junit.framework.TestCase;

public class testCreateFunction extends TestCase {

    public void testLanguagePython(){
        TGSqlParser sqlparser = new TGSqlParser(EDbVendor.dbvpostgresql);
        sqlparser.sqltext = "CREATE OR REPLACE FUNCTION public.s2_cellid_contains(a bigint, b bigint)\n" +
                " RETURNS boolean\n" +
                " LANGUAGE plpython3u\n" +
                " IMMUTABLE STRICT\n" +
                "AS $function$\n" +
                "  import s2sphere\n" +
                "  id_a = s2sphere.CellId(int.from_bytes(a.to_bytes(8, 'big', signed=True), 'big', signed=False))\n" +
                "  id_b = s2sphere.CellId(int.from_bytes(b.to_bytes(8, 'big', signed=True), 'big', signed=False))\n" +
                "  return id_a.contains(id_b)\n" +
                "$function$";
        assertTrue(sqlparser.parse() == 0);

        TPostgresqlCreateFunction createFunction = (TPostgresqlCreateFunction)sqlparser.sqlstatements.get(0);
        assertTrue(createFunction.getFunctionName().toString().equalsIgnoreCase("public.s2_cellid_contains"));
        assertTrue(createFunction.getParameterDeclarations().size() == 2);
        TParameterDeclaration parameterDeclaration = (TParameterDeclaration)createFunction.getParameterDeclarations().getParameterDeclarationItem(0);
        assertTrue(parameterDeclaration.getParameterName().toString().equalsIgnoreCase("a"));
        assertTrue(parameterDeclaration.getDataType().getDataType() == EDataType.bigint_t);
        assertTrue(createFunction.getReturnDataType().getDataType() == EDataType.boolean_t);
        assertTrue(createFunction.getProcedureLanguage().toString().equalsIgnoreCase("plpython3u"));
    }

    public void testDeclare(){
        TGSqlParser sqlparser = new TGSqlParser(EDbVendor.dbvpostgresql);
        sqlparser.sqltext = "CREATE FUNCTION somefunc() RETURNS integer AS $$\n" +
                "<< outerblock >>\n" +
                "DECLARE\n" +
                "    quantity integer := 30;\n" +
                "BEGIN\n" +
                "    RAISE NOTICE 'Quantity here is %', quantity;  -- Prints 30\n" +
                "    quantity := 50;\n" +
                "    --\n" +
                "    -- Create a subblock\n" +
                "    --\n" +
                "    DECLARE\n" +
                "        quantity integer := 80;\n" +
                "    BEGIN\n" +
                "        RAISE NOTICE 'Quantity here is %', quantity;  -- Prints 80\n" +
                "        RAISE NOTICE 'Outer quantity here is %', outerblock.quantity;  -- Prints 50\n" +
                "    END;\n" +
                "\n" +
                "    RAISE NOTICE 'Quantity here is %', quantity;  -- Prints 50\n" +
                "\n" +
                "    RETURN quantity;\n" +
                "END;\n" +
                "$$ LANGUAGE plpgsql;";
        assertTrue(sqlparser.parse() == 0);

        TPostgresqlCreateFunction createFunction = (TPostgresqlCreateFunction)sqlparser.sqlstatements.get(0);
        assertTrue(createFunction.getFunctionName().toString().equalsIgnoreCase("somefunc"));
        assertTrue(createFunction.getDeclareStatements().size() == 1);
        TVarDeclStmt declareVariable = (TVarDeclStmt)createFunction.getDeclareStatements().get(0);
        assertTrue(declareVariable.getElementName().toString().equalsIgnoreCase("quantity"));
        assertTrue(createFunction.getBodyStatements().size()==5);
    }

    public void test1(){
        TGSqlParser sqlparser = new TGSqlParser(EDbVendor.dbvpostgresql);
        sqlparser.sqltext = "CREATE FUNCTION sales_tax(subtotal int) RETURNS real AS $$\n" +
                "BEGIN\n" +
                "    RETURN subtotal * 0.06;\n" +
                "END;\n" +
                "$$ LANGUAGE plpgsql; ";
        assertTrue(sqlparser.parse() == 0);

        TPostgresqlCreateFunction createFunction = (TPostgresqlCreateFunction)sqlparser.sqlstatements.get(0);
        assertTrue(createFunction.getFunctionName().toString().equalsIgnoreCase("sales_tax"));
        assertTrue(createFunction.getParameterDeclarations().size() == 1);
        TParameterDeclaration parameterDeclaration = (TParameterDeclaration)createFunction.getParameterDeclarations().getParameterDeclarationItem(0);
        assertTrue(parameterDeclaration.getParameterName().toString().equalsIgnoreCase("subtotal"));
        assertTrue(parameterDeclaration.getDataType().getDataType() == EDataType.int_t);
        assertTrue(createFunction.getReturnDataType().getDataType() == EDataType.real_t);
        assertTrue(createFunction.getProcedureLanguage().toString().equalsIgnoreCase("plpgsql"));
    }

    public void testReturnTable(){
        TGSqlParser sqlparser = new TGSqlParser(EDbVendor.dbvpostgresql);
        sqlparser.sqltext = "CREATE FUNCTION extended_sales(p_itemno int)\n" +
                "RETURNS TABLE(quantity int, total numeric) AS $$\n" +
                "BEGIN\n" +
                "RETURN QUERY SELECT quantity, quantity\n" +
                "*\n" +
                "price FROM sales\n" +
                "WHERE itemno = p_itemno;\n" +
                "END;\n" +
                "$$ LANGUAGE plpgsql;";
        assertTrue(sqlparser.parse() == 0);

        TPostgresqlCreateFunction createFunction = (TPostgresqlCreateFunction)sqlparser.sqlstatements.get(0);
        assertTrue(createFunction.getFunctionName().toString().equalsIgnoreCase("extended_sales"));
        TParameterDeclaration parameterDeclaration = (TParameterDeclaration)createFunction.getParameterDeclarations().getParameterDeclarationItem(0);
        assertTrue(parameterDeclaration.getParameterName().toString().equalsIgnoreCase("p_itemno"));
        assertTrue(parameterDeclaration.getDataType().getDataType() == EDataType.int_t);
        assertTrue(createFunction.getReturnMode() == TBaseType.function_return_table);
        TTableElementList tls = createFunction.getReturnTableDefinitions();
        assertTrue(tls.size() == 2);

        TColumnDefinition cd = tls.getTableElement(0).getColumnDefinition();
        assertTrue(cd.getColumnName().toString().equalsIgnoreCase("quantity"));
        assertTrue(cd.getDatatype().getDataType() == EDataType.int_t);

        cd = tls.getTableElement(1).getColumnDefinition();
        assertTrue(cd.getColumnName().toString().equalsIgnoreCase("total"));
        assertTrue(cd.getDatatype().getDataType() == EDataType.numeric_t);

    }


    public void testDropTable(){
        TGSqlParser sqlparser = new TGSqlParser(EDbVendor.dbvpostgresql);
        sqlparser.sqltext = "CREATE OR REPLACE FUNCTION ibis_speedtestci_tmp.create_speedtest_marketshare_by_month_poa()\n" +
                " RETURNS void\n" +
                " LANGUAGE plpgsql\n" +
                "AS $function$\n" +
                "\n" +
                "DECLARE\n" +
                "begin\n" +
                "\n" +
                "drop table if exists ibis_speedtestci_tmp.speedtest_marketshare_by_month_poa;\n" +
                "create table ibis_speedtestci_tmp.speedtest_marketshare_by_month_poa\n" +
                "as\n" +
                "(\n" +
                "select date, postcode as postcode, network_operator_name, count(device_id) as unique_share\n" +
                "from (\n" +
                "  select postcode, network_operator_name, device_id, date_trunc('month', test_date) as date\n" +
                "  from ibis_speedtestci_raw.st_combined, ibis_admin_bdys_201811_raw.postcode_bdys_display\n" +
                "  where device_id != 0\n" +
                "  and sim_network_operator_code_a IS NOT NULL\n" +
                "  and sim_network_operator_code_a like '505__'\n" +
                "  and ST_Intersects(postcode_bdys_display.geom, st_combined.geom)\n" +
                "  group by postcode, network_operator_name, device_id, date_trunc('month', test_date)\n" +
                ") as st_combined\n" +
                "group by date, postcode, network_operator_name\n" +
                ");\n" +
                "\n" +
                "END;\n" +
                "\n" +
                "$function$";
        assertTrue(sqlparser.parse() == 0);

        TPostgresqlCreateFunction createFunction = (TPostgresqlCreateFunction)sqlparser.sqlstatements.get(0);
        assertTrue(createFunction.getFunctionName().toString().equalsIgnoreCase("ibis_speedtestci_tmp.create_speedtest_marketshare_by_month_poa"));
        assertTrue(createFunction.getProcedureLanguage().toString().equalsIgnoreCase("plpgsql"));
        assertTrue(createFunction.getBodyStatements().size()==2);
        assertTrue(createFunction.getBodyStatements().get(0).sqlstatementtype == ESqlStatementType.sstdroptable);
    }

    public void testIntoVariable(){
        TGSqlParser sqlparser = new TGSqlParser(EDbVendor.dbvpostgresql);
        sqlparser.sqltext = "CREATE OR REPLACE function \n" +
                "  totalrecords (emp_id INT) returns INTEGER \n" +
                "AS \n" +
                "  $total$ \n" +
                "  DECLARE total INTEGER;\n" +
                "  BEGIN \n" +
                "  SELECT total_sal \n" +
                "  INTO   total \n" +
                "  FROM   employee emp \n" +
                "  WHERE  emp.employee_id = emp_id;\n" +
                "  RETURN total;\n" +
                "  END;\n" +
                "  \n" +
                "  $total$ language plpgsql;";
        assertTrue(sqlparser.parse() == 0);

        TPostgresqlCreateFunction createFunction = (TPostgresqlCreateFunction)sqlparser.sqlstatements.get(0);
        assertTrue(createFunction.getFunctionName().toString().equalsIgnoreCase("totalrecords"));
        assertTrue(createFunction.getProcedureLanguage().toString().equalsIgnoreCase("plpgsql"));

        assertTrue(createFunction.getBodyStatements().size() == 2);
        TSelectSqlStatement selectSqlStatement = (TSelectSqlStatement)createFunction.getBodyStatements().get(0);
        TIntoClause intoClause = selectSqlStatement.getIntoClause();
       // System.out.println(intoClause.getExprList().getExpression(0).getExpressionType());
        //assertTrue(intoClause.getExprList().getExpression(0).getExpressionType() == EExpressionType.simple_object_name_t);
        TObjectName variableName = intoClause.getVariableList().getObjectName(0);
        assertTrue(variableName.getDbObjectType() == EDbObjectType.variable);
    }

    public void testCoordinates1(){
        TGSqlParser sqlparser = new TGSqlParser(EDbVendor.dbvpostgresql);
        sqlparser.sqltext = "CREATE OR REPLACE FUNCTION totalRecords (emp_id int)\n" +
                "RETURNS integer AS $total$\n" +
                "declare\n" +
                "total integer;\n" +
                "BEGIN\n" +
                "  SELECT TOTAL_SAL into total FROM employee emp where emp.employee_id = emp_id;\n" +
                "  RETURN total;\n" +
                "END; $total$ LANGUAGE plpgsql;";
        assertTrue(sqlparser.parse() == 0);

        TPostgresqlCreateFunction createFunction = (TPostgresqlCreateFunction)sqlparser.sqlstatements.get(0);
        assertTrue(createFunction.getFunctionName().toString().equalsIgnoreCase("totalrecords"));
        assertTrue(createFunction.getProcedureLanguage().toString().equalsIgnoreCase("plpgsql"));

        assertTrue(createFunction.getBodyStatements().size() == 2);
        TSelectSqlStatement selectSqlStatement = (TSelectSqlStatement)createFunction.getBodyStatements().get(0);
        TTable table = selectSqlStatement.tables.getTable(0);
        assertTrue(table.getTableName().getLineNo() == 6);
        assertTrue(table.getTableName().getColumnNo() == 36);
    }

    public void testCoordinates2(){
        TGSqlParser sqlparser = new TGSqlParser(EDbVendor.dbvpostgresql);
        sqlparser.sqltext = "CREATE OR REPLACE FUNCTION\n" +
                "foo.func1(integer, OUT f1 integer, OUT f2 text)\n" +
                "RETURNS record\n" +
                "LANGUAGE sql\n" +
                "AS $function$ SELECT $1, CAST($1 AS text) || ' is text' $function$";
        assertTrue(sqlparser.parse() == 0);

        functionVisitor fv = new functionVisitor();
        sqlparser.sqlstatements.get(0).acceptChildren(fv);
        assertTrue(fv.LineNo == 5);
        assertTrue(fv.ColumnNo == 26);

    }

    public void testCoordinates3(){
        TGSqlParser sqlparser = new TGSqlParser(EDbVendor.dbvpostgresql);
        sqlparser.sqltext = "CREATE OR REPLACE FUNCTION\n" +
                "foo.func1(integer, OUT f1 integer, OUT f2 text)\n" +
                "RETURNS record\n" +
                "LANGUAGE sql\n" +
                "AS $function$\n" +
                "SELECT $1, CAST($1 AS text) || ' is text' $function$";
        assertTrue(sqlparser.parse() == 0);

        functionVisitor fv = new functionVisitor();
        sqlparser.sqlstatements.get(0).acceptChildren(fv);
        assertTrue(fv.LineNo == 6);
        assertTrue(fv.ColumnNo == 12);

    }
}

class functionVisitor extends TParseTreeVisitor {

    public long LineNo, ColumnNo;
    public void preVisit(TFunctionCall functionCall){
        if (functionCall.getFunctionName().toString().equalsIgnoreCase("cast")){
            LineNo = functionCall.getFunctionName().getLineNo();
            ColumnNo = functionCall.getFunctionName().getColumnNo();
//            System.out.println(functionCall.getFunctionName().getLineNo());
//            System.out.println(functionCall.getFunctionName().getColumnNo());
        }
    }

}
