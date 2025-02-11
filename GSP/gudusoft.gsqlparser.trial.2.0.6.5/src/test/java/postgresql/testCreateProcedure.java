package postgresql;

import gudusoft.gsqlparser.EDataType;
import gudusoft.gsqlparser.EDbVendor;
import gudusoft.gsqlparser.ESqlStatementType;
import gudusoft.gsqlparser.TGSqlParser;
import gudusoft.gsqlparser.nodes.TParameterDeclaration;
import gudusoft.gsqlparser.stmt.TCreateProcedureStmt;
import gudusoft.gsqlparser.stmt.TUpdateSqlStatement;
import gudusoft.gsqlparser.stmt.postgresql.TPostgresqlCreateFunction;
import junit.framework.TestCase;

public class testCreateProcedure extends TestCase {

    public void test1(){
        TGSqlParser sqlparser = new TGSqlParser(EDbVendor.dbvpostgresql);
        sqlparser.sqltext = "CREATE OR REPLACE PROCEDURE transfer(INT, INT, DEC)\n" +
                "LANGUAGE plpgsql\n" +
                "AS $$\n" +
                "BEGIN\n" +
                "UPDATE accounts\n" +
                "   SET balance = balance - $3\n" +
                "   WHERE id = $1;\n" +
                "\n" +
                "   COMMIT;\n" +
                "END;\n" +
                "$$;";
        assertTrue(sqlparser.parse() == 0);

        TCreateProcedureStmt createProcedure = (TCreateProcedureStmt)sqlparser.sqlstatements.get(0);
        assertTrue(createProcedure.getProcedureName().toString().equalsIgnoreCase("transfer"));
        assertTrue(createProcedure.getParameterDeclarations().size() == 3);
        TParameterDeclaration parameterDeclaration = (TParameterDeclaration)createProcedure.getParameterDeclarations().getParameterDeclarationItem(0);
        assertTrue(parameterDeclaration.getDataType().getDataType() == EDataType.int_t);
        assertTrue(createProcedure.getProcedureLanguage().toString().equalsIgnoreCase("plpgsql"));

        assertTrue(createProcedure.getBodyStatements().size() == 2);
        TUpdateSqlStatement update = (TUpdateSqlStatement)createProcedure.getBodyStatements().get(0);
        assertTrue(update.getTargetTable().toString().equalsIgnoreCase("accounts"));

        //assertTrue(createProcedure.getBodyStatements().get(1).sqlstatementtype == ESqlStatementType.sstcommit);
        //System.out.print(createProcedure.getBodyStatements().get(1).sqlstatementtype );
    }
}
