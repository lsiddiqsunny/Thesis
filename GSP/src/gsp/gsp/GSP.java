package gsp;

import gudusoft.gsqlparser.*;
import gudusoft.gsqlparser.nodes.*;
import gudusoft.gsqlparser.nodes.TQueryHint;
import gudusoft.gsqlparser.stmt.*;
import gudusoft.gsqlparser.stmt.mssql.TMssqlSetRowCount;
import gudusoft.gsqlparser.stmt.mysql.*;
import gudusoft.gsqlparser.stmt.oracle.TBasicStmt;
import gudusoft.gsqlparser.stmt.oracle.TPlsqlCreatePackage;

import com.google.gson.*;

import java.io.*;
import java.util.ArrayList;

/*
 * original name of this demo was Parsing DDL,
 * renamed to analyzescript on 2011-11-09
 */

public class GSP{

    public static String toPrettyFormat(String jsonString)
    {
        JsonParser parser = new JsonParser();
        JsonObject json = parser.parse(jsonString).getAsJsonObject();

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String prettyJson = gson.toJson(json);

        return prettyJson;
    }

    static void parseQuery(String fileName){
        File file=new File(fileName);
        if (!file.exists()){
            System.out.println("File not exists:"+fileName);
            return ;
        }

        EDbVendor dbVendor = EDbVendor.dbvoracle;
        //String msg = "Please select SQL dialect: 1: SQL Server, 2: Oralce, 3: MySQL, 4: DB2, 5: PostGRESQL, 6: Teradata, default is 2: Oracle";
        //System.out.println(msg);

        dbVendor = EDbVendor.dbvoracle;

         /*try{
             int db = Integer.parseInt(br.readLine());
             if (db == 1){
                 dbVendor = EDbVendor.dbvmssql;
             }else if(db == 2){
                 dbVendor = EDbVendor.dbvoracle;
             }else if(db == 3){
                 dbVendor = EDbVendor.dbvmysql;
             }else if(db == 4){
                 dbVendor = EDbVendor.dbvdb2;
             }else if(db == 5){
                 dbVendor = EDbVendor.dbvpostgresql;
             }else if(db == 6){
                 dbVendor = EDbVendor.dbvteradata;
             }
         }catch(IOException i) {
         }catch (NumberFormatException numberFormatException){
         }*/

        System.out.println("Selected SQL dialect: "+dbVendor.toString());

        TGSqlParser sqlparser = new TGSqlParser(dbVendor);

        sqlparser.sqlfilename  = fileName;

        int ret = sqlparser.parse();
        if (ret == 0){
            for(int i=0;i<sqlparser.sqlstatements.size();i++){
                System.out.println("Query:\n\n"+sqlparser.sqlstatements.get(i)+"\n\nParsed Stmt:\n");
                String json = "{";
                String parsed = analyzeStmt(sqlparser.sqlstatements.get(i));
                if(parsed==null){
                    continue;
                }
                json+=parsed;
                json += "}";
                System.out.println("");
                System.out.println(toPrettyFormat(json)+"\n\n");

            }
        }else{
            System.out.println(sqlparser.getErrormessage());
            return;
        }
    }
    public static void main(String[] args) throws IOException {
        parseQuery("test.sql");
        //System.out.println(parsed);
        /*

        //String directory = "D:\\Thesis\\GSP\\Dataset from Jahin\\";
        String directory = "D:\\Thesis\\GSP\\Dataset from Jahin\\";
        File directoryPath = new File(directory);
        String[] contents = directoryPath.list();
        System.out.println("List of files and directories in the specified directory:");
        for(int i=0; i<contents.length; i++) {
            File file = new File(directory+contents[i]);
            if(file.isDirectory()){
                String[] fileContents = file.list();
                int j=0;
                System.out.println(file.getName());
                for(String fileName:fileContents){
                    if(fileName.contains("Query")){
                        j++;
                        String parsed = parseQuery(directory+contents[i]+"//"+fileName);
                        System.out.println(parsed);
                        FileWriter myWriter = new FileWriter(directory+contents[i]+"//"+"Parsed"+j+".json");
                        myWriter.write(parsed);
                        myWriter.close();
                    }
                }

            }
        }*/




    }

    protected static String analyzeStmt(TCustomSqlStatement stmt){

        switch(stmt.sqlstatementtype){
            case sstselect:
                return analyzeSelectStmt((TSelectSqlStatement)stmt);
            // break;
            case sstupdate:
                return analyzeUpdateStmt((TUpdateSqlStatement)stmt);
            //break;
            case sstinsert:
                return analyzeInsertStmt((TInsertSqlStatement)stmt);
            //break;
            case sstdelete:
                return analyzeDeleteStmt((TDeleteSqlStatement)stmt);
            //break;
            case sstcreatetable:
                analyzeCreateTableStmt((TCreateTableSqlStatement)stmt);
                break;
            case sstaltertable:
                analyzeAlterTableStmt((TAlterTableStatement) stmt);
                break;
            case sstcreateview:
                analyzeCreateViewStmt((TCreateViewSqlStatement)stmt);
                break;
            case sstcreateindex:
                analyzeCreateIndexStmt((TCreateIndexSqlStatement)stmt);
                break;
            case sstplsql_createpackage:
                analyzePlsqlPackage((TPlsqlCreatePackage)stmt);
                break;
            case sstmysqlcreatefunction:
                analyzemysqlfunction((TMySQLCreateFunction) stmt);
                break;
            case sstmysqldeclare:
                analyzemysqldeclare((TMySQLDeclare)stmt);
                break;
            case sstmysqlreturn:
                analyzemysqlReturn((TMySQLReturn)stmt);
                break;
            case sstmysqlifstmt:
                analyzemysqlif((TMySQLIfStmt)stmt);
                break;
            case sstmssqlsetrowcount:
                System.out.println("set rowcount statement, number is:"+((TMssqlSetRowCount)stmt).getNumberExpr().toString());
                break;
            case sst_block_with_label:
                analyzeplsqlblock((TCommonBlock) stmt);
                break;
            default:
                System.out.println(stmt.sqlstatementtype.toString());
                System.out.println(stmt.toString());
        }
        return null;
    }

    protected static void  analyzemysqldeclare(TMySQLDeclare pStmt){
        System.out.println("declare:"+pStmt.getDeclareType());
    }

    protected static void  analyzeBasicStmt(TBasicStmt stmt){
        System.out.println(stmt.getExpr().getExpressionType());
        //System.out.println(stmt.getExpr().toString());
        switch (stmt.getExpr().getExpressionType()){
            case function_t:
                TFunctionCall functionCall = stmt.getExpr().getFunctionCall();
                System.out.println(functionCall.getFunctionName().toString());
                System.out.println("object name:"+functionCall.getFunctionName().getObjectToken().toString());
                if (functionCall.getFunctionName().getSchemaToken() != null){
                    System.out.print("schema name:"+functionCall.getFunctionName().getSchemaToken().toString());
                }
                if (functionCall.getFunctionName().getDatabaseToken() != null){
                    System.out.println("database name:"+functionCall.getFunctionName().getDatabaseToken().toString());
                }
                break;
            default:
        }
    }

    protected static void  analyzeplsqlblock(TCommonBlock stmt){
        for (int i=0;i<stmt.getBodyStatements().size();i++){
            switch (stmt.getBodyStatements().get(i).sqlstatementtype){
                case sstplsql_procbasicstmt:
                    analyzeBasicStmt((TBasicStmt)(stmt.getBodyStatements().get(i)));
                    break;
                default:
                    System.out.println(stmt.getBodyStatements().get(i).sqlstatementtype);
            }

        }
    }
    protected static void  analyzemysqlif(TMySQLIfStmt pStmt){
        System.out.println("if condition:"+pStmt.getCondition().toString());

        if (pStmt.getThenStmts().size() > 0){
            System.out.println("then statements;");
            for(int i=0;i<pStmt.getThenStmts().size();i++){
                analyzeStmt(pStmt.getThenStmts().get(i));
            }
        }

        if (pStmt.getDefaultStmts().size() > 0){
            System.out.println("else statements;");
            for(int i=0;i<pStmt.getDefaultStmts().size();i++){
                analyzeStmt(pStmt.getDefaultStmts().get(i));
            }
        }
    }

    protected static void analyzeTableHint(TTableHint th){
        System.out.println("Table hint:");
        if (th.isIndex()){
            System.out.println("hint: index");
            if (th.getExprList() != null){
                for(int j=0;j<th.getExprList().size();j++){
                    System.out.println("Index value:"+th.getExprList().getElement(j).toString());
                }
            }else{
                System.out.println("Index value:"+th.getHint().toString());
            }
        }else{
            System.out.println("hint: "+th.getHint().toString());
        }
    }
    protected static void analyzeTable(TTable table){
        System.out.format("Table Type: %s\n",table.getTableType().toString());
        switch (table.getTableType()){
            case objectname:
                System.out.format("Table name: %s\n",table.getTableName().toString());
                break;
            case subquery:
                analyzeSelectStmt(table.getSubquery());
                break;
            default:
                break;
        }
        if (table.getAliasClause() != null){
            System.out.format("Table alias: %s\n",table.getAliasClause().toString()) ;
        }
        if (table.getTableHintList() != null){
            for(int i=0;i<table.getTableHintList().size();i++){
                TTableHint th = table.getTableHintList().getElement(i);
                analyzeTableHint(th);
            }
        }
    }

    protected static void printConstraint(TConstraint constraint, Boolean outline){

        if (constraint.getConstraintName() != null){
            System.out.println("\t\tconstraint name:"+constraint.getConstraintName().toString());
        }

        switch(constraint.getConstraint_type()){
            case notnull:
                System.out.println("\t\tnot null");
                break;
            case primary_key:
                System.out.println("\t\tprimary key");
                if (outline){
                    String lcstr = "";
                    if (constraint.getColumnList() != null){
                        for(int k=0;k<constraint.getColumnList().size();k++){
                            if (k !=0 ){lcstr = lcstr+",";}
                            lcstr = lcstr+constraint.getColumnList().getElement(k).getColumnName().toString();
                        }
                        System.out.println("\t\tprimary key columns:"+lcstr);
                    }
                }
                break;
            case unique:
                System.out.println("\t\tunique key");
                if(outline){
                    String lcstr="";
                    if (constraint.getColumnList() != null){
                        for(int k=0;k<constraint.getColumnList().size();k++){
                            if (k !=0 ){lcstr = lcstr+",";}
                            lcstr = lcstr+constraint.getColumnList().getElement(k).getColumnName().toString();
                        }
                    }
                    System.out.println("\t\tcolumns:"+lcstr);
                }
                break;
            case check:
                System.out.println("\t\tcheck:"+constraint.getCheckCondition().toString());
                break;
            case foreign_key:
            case reference:
                System.out.println("\t\tforeign key");
                if(outline){
                    String lcstr="";
                    if (constraint.getColumnList() != null){
                        for(int k=0;k<constraint.getColumnList().size();k++){
                            if (k !=0 ){lcstr = lcstr+",";}
                            lcstr = lcstr+constraint.getColumnList().getElement(k).getColumnName().toString();
                        }
                    }
                    System.out.println("\t\tcolumns:"+lcstr);
                }
                System.out.println("\t\treferenced table:"+constraint.getReferencedObject().toString());
                if (constraint.getReferencedColumnList() != null){
                    String lcstr="";
                    for(int k=0;k<constraint.getReferencedColumnList().size();k++){
                        if (k !=0 ){lcstr = lcstr+",";}
                        lcstr = lcstr+constraint.getReferencedColumnList().getObjectName(k).toString();
                    }
                    System.out.println("\t\treferenced columns:"+lcstr);
                }
                break;
            default:
                break;
        }
    }

    protected static void printObjectNameList(TObjectNameList objList){
        for(int i=0;i<objList.size();i++){
            System.out.println(objList.getObjectName(i).toString());
        }
    }

    protected static void printObjectNameWithSort(TPTNodeList<TColumnWithSortOrder > sortColumnList){
        for(int i=0;i<sortColumnList.size();i++){
            System.out.println(sortColumnList.getElement(i).getColumnName().toString());
        }
    }


    protected static void printColumnDefinitionList(TColumnDefinitionList cdl){
        for(int i=0;i<cdl.size();i++){
            System.out.println(cdl.getColumn(i).getColumnName());
        }
    }
    protected static void printConstraintList(TConstraintList cnl){
        for(int i=0;i<cnl.size();i++){
            printConstraint(cnl.getConstraint(i),true);
        }
    }

    protected static void printAlterTableOption(TAlterTableOption ato){
        System.out.println(ato.getOptionType());
        switch (ato.getOptionType()){
            case AddColumn:
                printColumnDefinitionList(ato.getColumnDefinitionList());
                break;
            case ModifyColumn:
                printColumnDefinitionList(ato.getColumnDefinitionList());
                break;
            case AlterColumn:
                System.out.println(ato.getColumnName().toString());
                break;
            case DropColumn:
                System.out.println(ato.getColumnName().toString());
                break;
            case SetUnUsedColumn:  //oracle
                printObjectNameList(ato.getColumnNameList());
                break;
            case DropUnUsedColumn:
                break;
            case DropColumnsContinue:
                break;
            case RenameColumn:
                System.out.println("rename "+ato.getColumnName().toString()+" to "+ato.getNewColumnName().toString());
                break;
            case ChangeColumn:   //MySQL
                System.out.println(ato.getColumnName().toString());
                System.out.println(ato.getNewColumnDef().toString());
                //printColumnDefinitionList(ato.getColumnDefinitionList());
                break;
            case RenameTable:   //MySQL
                System.out.println(ato.getColumnName().toString());
                break;
            case AddConstraint:
                printConstraintList(ato.getConstraintList());
                break;
            case AddConstraintIndex:    //MySQL
                if (ato.getColumnName() != null){
                    System.out.println(ato.getColumnName().toString());
                }
                printObjectNameList(ato.getColumnNameList());
                break;
            case AddConstraintPK:
            case AddConstraintUnique:
            case AddConstraintFK:
                if (ato.getConstraintName() != null){
                    System.out.println(ato.getConstraintName().toString());
                }
                if (ato.getIndexCols() != null){
                    printObjectNameWithSort(ato.getIndexCols());
                }else{
                    printObjectNameList(ato.getColumnNameList());
                }

                break;
            case ModifyConstraint:
                System.out.println(ato.getConstraintName().toString());
                break;
            case RenameConstraint:
                System.out.println("rename "+ato.getConstraintName().toString()+" to "+ato.getNewConstraintName().toString());
                break;
            case DropConstraint:
                System.out.println(ato.getConstraintName().toString());
                break;
            case DropConstraintPK:
                break;
            case DropConstraintFK:
                System.out.println(ato.getConstraintName().toString());
                break;
            case DropConstraintUnique:
                if (ato.getConstraintName() != null){ //db2
                    System.out.println(ato.getConstraintName());
                }

                if (ato.getColumnNameList() != null){//oracle
                    printObjectNameList(ato.getColumnNameList());
                }
                break;
            case DropConstraintCheck: //db2
                System.out.println(ato.getConstraintName());
                break;
            case DropConstraintPartitioningKey:
                break;
            case DropConstraintRestrict:
                break;
            case DropConstraintIndex:
                System.out.println(ato.getConstraintName());
                break;
            case DropConstraintKey:
                System.out.println(ato.getConstraintName());
                break;
            case AlterConstraintFK:
                System.out.println(ato.getConstraintName());
                break;
            case AlterConstraintCheck:
                System.out.println(ato.getConstraintName());
                break;
            case CheckConstraint:
                break;
            case OraclePhysicalAttrs:
            case toOracleLogClause:
            case OracleTableP:
            case MssqlEnableTrigger:
            case MySQLTableOptons:
            case Db2PartitioningKeyDef:
            case Db2RestrictOnDrop:
            case Db2Misc:
            case Unknown:
                break;
        }

    }

    protected static void analyzeParameter(TParameterDeclaration param){
        System.out.println("name:" + param.getParameterName().toString() + ",datatype:" + param.getDataType().toString());
    }

    protected static void analyzeParameters(TParameterDeclarationList params){

        if (params.size() == 0) return;
        for(int i=0;i<params.size();i++){
            analyzeParameter(params.getParameterDeclarationItem(i));
        }
    }

    protected static void analyzemysqlblock(TMySQLBlock pStmt){
        if (pStmt.getBodyStatements().size() == 0) return;
        System.out.println("statements inside body:"+pStmt.getBodyStatements().size());
        for(int i=0;i<pStmt.getBodyStatements().size();i++){
            analyzeStmt(pStmt.getBodyStatements().get(i));
        }
    }
    protected static void analyzemysqlReturn(TMySQLReturn pStmt){
        System.out.println("return value:"+pStmt.getReturnExpr().toString());
    }

    protected static void analyzemysqlfunction(TMySQLCreateFunction pStmt){
        System.out.println("function name:"+pStmt.getFunctionName().toString());
        if (pStmt.getParameterDeclarations() != null){
            analyzeParameters(pStmt.getParameterDeclarations());
        }
        // System.out.println("parameters:" + an);
        System.out.println("declare statements:"+pStmt.getDeclareStatements().size());
        System.out.println("body statements:"+pStmt.getBodyStatements().size());
        if (pStmt.getBodyStatements().get(0).sqlstatementtype == ESqlStatementType.sstmysqlblock){
            analyzemysqlblock((TMySQLBlock)pStmt.getBodyStatements().get(0));
        }else if (pStmt.getBodyStatements().get(0).sqlstatementtype == ESqlStatementType.sstmysqlreturn){
            analyzemysqlReturn((TMySQLReturn)pStmt.getBodyStatements().get(0));
        }

    }

    protected static void analyzePlsqlPackage(TPlsqlCreatePackage pStmt){
        System.out.println("package name:"+pStmt.getPackageName().toString());
        System.out.println("declare statements:"+pStmt.getDeclareStatements().size());
        System.out.println("body statements:"+pStmt.getBodyStatements().size());
    }

    protected static void analyzeCreateIndexStmt(TCreateIndexSqlStatement pStmt){
        TCreateIndexSqlStatement createIndex = pStmt;
        System.out.println("Index name:"+createIndex.getIndexName().toString());
        System.out.println("Table name:"+createIndex.getTableName().toString());
        TOrderByItemList list = createIndex.getColumnNameList();
        if (list != null){
            for(int i=0;i<list.size();i++){
                System.out.println("\tcolumn name:" + list.getOrderByItem(i).getSortKey().toString());
            }
        }

    }

    protected static void analyzeCreateViewStmt(TCreateViewSqlStatement pStmt){
        TCreateViewSqlStatement createView = pStmt;
        System.out.println("View name:"+createView.getViewName().toString());
        if (createView.getViewAliasClause() != null){
            TViewAliasClause aliasClause = createView.getViewAliasClause();
            for(int i=0;i<aliasClause.getViewAliasItemList().size();i++){
                System.out.println("View alias:"+aliasClause.getViewAliasItemList().getViewAliasItem(i).toString());
            }
        }

        //System.out.println("View subquery: \n"+ createView.getSubquery().toString() );
        analyzeSelectStmt(createView.getSubquery());
    }
    static class Column{
        private final String columnName;
        private final String columnAlias;
        Column(String columnName,String columnAlias){
            this.columnName=columnName;
            this.columnAlias=columnAlias;
        }
    }
    static class Table{
        private final String tableName;
        private final String tableAlias;

        Table(String tableName,String tableAlias){
            this.tableName=tableName;
            this.tableAlias=tableAlias;
        }
    }

    static class Join{
        private final String joinType;

        private final Table table;
        private final ArrayList<Join> sourceJoinItems;
        private final ArrayList<Join> joinItems;

        public String on ="";
        public String using ="";
        Join(String joinType, Table table, ArrayList<Join> sourceJoinItems, ArrayList<Join> joinItems){
            this.joinType=joinType;
            this.table = table;
            this.sourceJoinItems = sourceJoinItems;
            this.joinItems = joinItems;
        }
    }
    static class Expr{
        public String expr;
        public String alias;


    }

    static class Condition{
        public String type;
        public Condition leftCondition;
        public Condition rightCondition;
        public Expr leftExpression;
        public Expr rightExpression;
        Condition(){
            type="";


        }
    }
    protected static String analyzeWhereStmt(TExpression pStmt){
        String jsonString="";
        if(pStmt.isLeaf()) {

            Gson gson = new Gson();
            Expr expr =  new Expr();
            expr.expr =pStmt.toString();
            expr.alias = pStmt.getExprAlias()==null?"": pStmt.getExprAlias().toString();
            jsonString+=gson.toJson(expr);
            System.out.println("expr: "+pStmt.toString()+"  alias: "+pStmt.getExprAlias());

            return jsonString;
        }
        else{
            Condition cond = new Condition();
            if(!pStmt.getExpressionType().toString().equals("simple_comparison_t")){
                System.out.println("condition: "+pStmt.getExpressionType());
                cond.type=pStmt.getExpressionType().toString();

            }
            else
            {System.out.println("condition: "+pStmt.getComparisonType());
                cond.type=pStmt.getComparisonType().toString();
            }
            Gson gson = new Gson();
            if(pStmt.getLeftOperand() != null)
            {
                if(pStmt.getLeftOperand().isLeaf()){
                    cond.leftExpression = gson.fromJson(analyzeWhereStmt(pStmt.getLeftOperand()),Expr.class);
                }
                else
                    cond.leftCondition = gson.fromJson(analyzeWhereStmt(pStmt.getLeftOperand()),Condition.class);
            }


            if(pStmt.getRightOperand() != null)
            {
                if(pStmt.getRightOperand().isLeaf()){
                    cond.rightExpression = gson.fromJson(analyzeWhereStmt(pStmt.getRightOperand()),Expr.class);
                }
                else
                    cond.rightCondition = gson.fromJson(analyzeWhereStmt(pStmt.getRightOperand()),Condition.class);
            }


            jsonString+=gson.toJson(cond);
            return jsonString;
        }

    }

    protected static String analyzeSelectStmt(TSelectSqlStatement pStmt){
        String select="";
        select+="\"Select\":{";
        System.out.println("\nSelect:");
        if (pStmt.isCombinedQuery()){
            String setstr="";
            switch (pStmt.getSetOperator()){
                case 1: setstr = "union";break;
                case 2: setstr = "union all";break;
                case 3: setstr = "intersect";break;
                case 4: setstr = "intersect all";break;
                case 5: setstr = "minus";break;
                case 6: setstr = "minus all";break;
                case 7: setstr = "except";break;
                case 8: setstr = "except all";break;
            }
            // select+=setstr;
            System.out.printf("set type: %s\n",setstr);
            System.out.println("left select:");
            analyzeSelectStmt(pStmt.getLeftStmt());
            System.out.println("right select:");
            analyzeSelectStmt(pStmt.getRightStmt());
            if (pStmt.getOrderbyClause() != null){
                System.out.printf("order by clause %s\n",pStmt.getOrderbyClause().toString());
            }
        }else{
            //select list
            Gson gson = new Gson();
            // select+=gson.toJson(pStmt.getResultColumnList());
            select+="\"Column\": ";
            ArrayList<Column> columns = new ArrayList<>();
            for(int i=0; i < pStmt.getResultColumnList().size();i++){
                TResultColumn resultColumn = pStmt.getResultColumnList().getResultColumn(i);
                String columnName= resultColumn.getExpr().toString();
                String aliasName = (resultColumn.getAliasClause() == null)?"":resultColumn.getAliasClause().toString();

                Column column = new Column(columnName,aliasName);
                columns.add(column);
                System.out.printf("Column: %s, Alias: %s\n",resultColumn.getExpr().toString(), (resultColumn.getAliasClause() == null)?"":resultColumn.getAliasClause().toString());
            }
            select+=gson.toJson(columns);
            //from clause, check this document for detailed information
            //http://www.sqlparser.com/sql-parser-query-join-table.php
            gson = new Gson();


            select+=",\"Join\":";
            ArrayList<Join> joins = new ArrayList<>();
            for(int i=0;i<pStmt.joins.size();i++){
                TJoin join = pStmt.joins.getJoin(i);
                Table table =null;
                Join joinTable = null;
                ArrayList<Join> sourceJoinItems = new ArrayList<>();
                ArrayList<Join> joinItems = new ArrayList<>();
                switch (join.getKind()){
                    case TBaseType.join_source_fake:
                        analyzeTable(join.getTable());
                        String tableName= join.getTable().toString();
                        String aliasName = (join.getTable().getAliasClause() !=null)?join.getTable().getAliasClause().toString():"";

                        table = new Table(tableName,aliasName);

                        joinTable = new Join("join_source_fake",table, sourceJoinItems, joinItems);
                        joins.add(joinTable);


                        System.out.printf("table: %s, alias: %s\n",join.getTable().toString(),(join.getTable().getAliasClause() !=null)?join.getTable().getAliasClause().toString():"");
                        break;
                    case TBaseType.join_source_table:
                        //System.out.println("here");
                        analyzeTable(join.getTable());

                        System.out.printf("table: %s, alias: %s\n",join.getTable().toString(),(join.getTable().getAliasClause() !=null)?join.getTable().getAliasClause().toString():"");

                        for(int j=0;j<join.getJoinItems().size();j++){
                            TJoinItem joinItem = join.getJoinItems().getJoinItem(j);
                            System.out.printf("Join type: %s\n",joinItem.getJoinType().toString());
                            analyzeTable(joinItem.getTable());
                            tableName= joinItem.getTable().toString();
                            aliasName = (joinItem.getTable().getAliasClause() !=null)?joinItem.getTable().getAliasClause().toString():"";

                            table = new Table(tableName,aliasName);
                            Join joinJson = new Join(joinItem.getJoinType().toString(),table, new ArrayList<>(), new ArrayList<>());

                            System.out.printf("table: %s, alias: %s\n",joinItem.getTable().toString(),(joinItem.getTable().getAliasClause() !=null)?joinItem.getTable().getAliasClause().toString():"");
                            if (joinItem.getOnCondition() != null){
                                System.out.printf("On: %s\n",joinItem.getOnCondition().toString());
                                joinJson.on=joinItem.getOnCondition().toString();
                            }else  if (joinItem.getUsingColumns() != null){
                                System.out.printf("using: %s\n",joinItem.getUsingColumns().toString());
                                joinJson.using =joinItem.getUsingColumns().toString();
                            }
                            joinItems.add(joinJson);
                        }
                        tableName= join.getTable().toString();
                        aliasName = (join.getTable().getAliasClause() !=null)?join.getTable().getAliasClause().toString():"";

                        table = new Table(tableName,aliasName);
                        joinTable = new Join("join_source_table",table, sourceJoinItems, joinItems);
                        joins.add(joinTable);
                        break;
                    case TBaseType.join_source_join:
                        TJoin source_join = join.getJoin();
                        analyzeTable(source_join.getTable());

                        System.out.printf("table: %s, alias: %s\n",source_join.getTable().toString(),(source_join.getTable().getAliasClause() !=null)?source_join.getTable().getAliasClause().toString():"");

                        for(int j=0;j<source_join.getJoinItems().size();j++){
                            TJoinItem joinItem = source_join.getJoinItems().getJoinItem(j);
                            System.out.printf("source_join type: %s\n",joinItem.getJoinType().toString());
                            analyzeTable(joinItem.getTable());
                            tableName= joinItem.getTable().toString();
                            aliasName = (joinItem.getTable().getAliasClause() !=null)?joinItem.getTable().getAliasClause().toString():"";

                            table = new Table(tableName,aliasName);
                            Join joinJson = new Join(joinItem.getJoinType().toString(),table, new ArrayList<>(), new ArrayList<>());

                            System.out.printf("table: %s, alias: %s\n",joinItem.getTable().toString(),(joinItem.getTable().getAliasClause() !=null)?joinItem.getTable().getAliasClause().toString():"");
                            if (joinItem.getOnCondition() != null){
                                System.out.printf("On: %s\n",joinItem.getOnCondition().toString());
                                joinJson.on=joinItem.getOnCondition().toString();
                            }else  if (joinItem.getUsingColumns() != null){
                                System.out.printf("using: %s\n",joinItem.getUsingColumns().toString());
                                joinJson.using =joinItem.getUsingColumns().toString();
                            }
                            sourceJoinItems.add(joinJson);
                        }

                        for(int j=0;j<join.getJoinItems().size();j++){
                            TJoinItem joinItem = join.getJoinItems().getJoinItem(j);
                            System.out.printf("Join type: %s\n",joinItem.getJoinType().toString());
                            analyzeTable(joinItem.getTable());
                            tableName= joinItem.getTable().toString();
                            aliasName = (joinItem.getTable().getAliasClause() !=null)?joinItem.getTable().getAliasClause().toString():"";

                            table = new Table(tableName,aliasName);
                            Join joinJson = new Join(joinItem.getJoinType().toString(),table, new ArrayList<>(), new ArrayList<>());


                            System.out.printf("table: %s, alias: %s\n",joinItem.getTable().toString(),(joinItem.getTable().getAliasClause() !=null)?joinItem.getTable().getAliasClause().toString():"");
                            if (joinItem.getOnCondition() != null){
                                System.out.printf("On: %s\n",joinItem.getOnCondition().toString());
                                joinJson.on=joinItem.getOnCondition().toString();
                            }else  if (joinItem.getUsingColumns() != null){
                                System.out.printf("using: %s\n",joinItem.getUsingColumns().toString());
                                joinJson.using =joinItem.getUsingColumns().toString();
                            }
                            joinItems.add(joinJson);
                        }
                        tableName= join.getTable().toString();
                        aliasName = (join.getTable().getAliasClause() !=null)?join.getTable().getAliasClause().toString():"";

                        table = new Table(tableName,aliasName);
                        joinTable = new Join("join_source_join",table, sourceJoinItems, joinItems);
                        joins.add(joinTable);
                        break;
                    default:
                        System.out.println("unknown type in join!");
                        break;
                }
            }
            select+=gson.toJson(joins);
            //where clause
            if (pStmt.getWhereClause() != null){
                select+=",\"where clause\":"+analyzeWhereStmt(pStmt.getWhereClause().getCondition());
                System.out.printf("where clause: \n%s\n", pStmt.getWhereClause().toString());
                //System.out.println(analyzeWhereStmt(pStmt.getWhereClause().getCondition()));

                //for(Object x: pStmt.getWhereClause().getCondition().getExprList().size())
            }

            // group by
            if (pStmt.getGroupByClause() != null){
                select+=",\"group by\":\""+pStmt.getGroupByClause().toString()+"\"";
                System.out.printf("group by: \n%s\n",pStmt.getGroupByClause().toString());
            }

            // order by
            if (pStmt.getOrderbyClause() != null){
                select+=",\"order by\":" ;
                ArrayList<String> orderBy = new ArrayList<>();
                for(int i=0;i<pStmt.getOrderbyClause().getItems().size();i++){
                    orderBy.add(pStmt.getOrderbyClause().getItems().getOrderByItem(i).toString());
                }
                select+=gson.toJson(orderBy);

                System.out.printf("order by: \n%s\n",pStmt.getOrderbyClause().toString());
            }

            // for update
            if (pStmt.getForUpdateClause() != null){
                select+=",\"for update\":\""+pStmt.getForUpdateClause().toString()+"\"";
                System.out.printf("for update: \n%s\n",pStmt.getForUpdateClause().toString());
            }

            // top clause
            if (pStmt.getTopClause() != null){
                select+=",\"top clause\":\""+pStmt.getTopClause().toString()+"\"";
                System.out.printf("top clause: \n%s\n",pStmt.getTopClause().toString());
            }

            // limit clause
            if (pStmt.getLimitClause() != null){
                select+=",\"limit clause\":\""+pStmt.getLimitClause().toString()+"\"";
                System.out.printf("limit clause: \n%s\n",pStmt.getLimitClause().toString());
            }

            if (pStmt.getOptionClause() != null){
                for(int k=0;k<pStmt.getOptionClause().getQueryHints().size();k++){
                    TQueryHint qh = pStmt.getOptionClause().getQueryHints().getElement(k);
                    System.out.println("query hint type:"+qh.getQueryHintType());
                    switch (qh.getQueryHintType()){
                        case E_QUERY_HINT_TABLE_HINT:
                            System.out.println("exposed_object_name:"+qh.getExposed_object_name().toString());
                            for(int m=0;m<qh.getTableHints().size();m++){
                                TTableHint th = qh.getTableHints().getElement(m);
                                analyzeTableHint(th);
                            }
                            break;
                        default:
                            break;
                    }
                }
            }
        }
        select+="}";
        return select;
        //return null;
    }

    protected static String analyzeInsertStmt(TInsertSqlStatement pStmt){
        String insert = "\"Insert\":{";
        if (pStmt.getTargetTable() != null){
            insert+="\"tableName\":\""+pStmt.getTargetTable().toString()+"\",";
            insert+="\"tableAlias\":\""+"\"";
            System.out.println("Table name:"+pStmt.getTargetTable().toString());
        }

        System.out.println("insert value type:"+pStmt.getValueType());

        if (pStmt.getColumnList() != null){
            Gson gson = new Gson();
            // select+=gson.toJson(pStmt.getResultColumnList());
            if (pStmt.getTargetTable() != null)
                insert+=",";
            insert+="\"Column\": ";
            ArrayList<Column> columns = new ArrayList<>();
            System.out.println("columns:");
            for(int i=0;i<pStmt.getColumnList().size();i++){
                Column col = new Column(pStmt.getColumnList().getObjectName(i).toString(),"");
                System.out.println("\t"+pStmt.getColumnList().getObjectName(i).toString());
                columns.add(col);
            }
            insert+=gson.toJson(columns);
        }

        if (pStmt.getValues() != null){
            Gson gson = new Gson();
            // select+=gson.toJson(pStmt.getResultColumnList());
            insert+=",";

            insert+="\"Values\": ";
            ArrayList<String> values = new ArrayList<>();
            System.out.println("values:");
            for(int i=0;i<pStmt.getValues().size();i++){
                TMultiTarget mt = pStmt.getValues().getMultiTarget(i);
                for(int j=0;j<mt.getColumnList().size();j++) {
                    values.add(mt.getColumnList().getResultColumn(j).toString());
                    System.out.println("\t" + mt.getColumnList().getResultColumn(j).toString());
                }
            }
            insert+=gson.toJson(values);
        }

        if (pStmt.getSubQuery() != null){
            insert+=","+analyzeSelectStmt(pStmt.getSubQuery());
        }
        insert+="}";
        return insert;
    }
    static class Expression{
        private final String column;
        private final String value;
        Expression(String left,String right){
            this.column=left;
            this.value=right;
        }
    }
    protected static String analyzeDeleteStmt(TDeleteSqlStatement pStmt){
        String delete = "\"Delete\":{";
        if (pStmt.getTargetTable() != null){
            delete+="\"tableName\":\""+pStmt.getTargetTable().toString()+"\",";
            delete+="\"tableAlias\":\""+"\"";
            System.out.println("Table name:"+pStmt.getTargetTable().toString());
        }
        if (pStmt.getWhereClause() != null){
            delete+=",\"where clause\":"+analyzeWhereStmt(pStmt.getWhereClause().getCondition());
            System.out.printf("where clause: \n%s\n", pStmt.getWhereClause().toString());
            analyzeWhereStmt(pStmt.getWhereClause().getCondition());
        }
        delete+="}";
        return delete;
    }
    protected static String analyzeUpdateStmt(TUpdateSqlStatement pStmt){
        String update = "\"Update\":{";
        if (pStmt.getTargetTable() != null){
            update+="\"tableName\":\""+pStmt.getTargetTable().toString()+"\",";
            update+="\"tableAlias\":\""+"\"";
            System.out.println("Table name:"+pStmt.getTargetTable().toString());
        }

        update+=",\"set clause\": ";
        ArrayList<Expression> expressions = new ArrayList<>();

        System.out.println("set clause:");
        for(int i=0;i<pStmt.getResultColumnList().size();i++){
            TResultColumn resultColumn = pStmt.getResultColumnList().getResultColumn(i);
            TExpression expression = resultColumn.getExpr();
            Expression expr = new Expression(expression.getLeftOperand().toString(),expression.getRightOperand().toString());
            expressions.add(expr);
            System.out.println("\tcolumn:"+expression.getLeftOperand().toString()+"\tvalue:"+expression.getRightOperand().toString());
        }
        Gson gson = new Gson();
        update+=gson.toJson(expressions);
        if(pStmt.getWhereClause() != null){
            update+=",\"where clause\":"+analyzeWhereStmt(pStmt.getWhereClause().getCondition());

            analyzeWhereStmt(pStmt.getWhereClause().getCondition());
            System.out.println("where clause:\n"+pStmt.getWhereClause().getCondition().toString());
        }
        update+="}";
        return update;
    }

    protected static void analyzeAlterTableStmt(TAlterTableStatement pStmt){
        System.out.println("Table Name:"+pStmt.getTableName().toString());
        System.out.println("Alter table options:");
        for(int i=0;i<pStmt.getAlterTableOptionList().size();i++){
            printAlterTableOption(pStmt.getAlterTableOptionList().getAlterTableOption(i));
        }
    }

    protected static void analyzeCreateTableStmt(TCreateTableSqlStatement pStmt){
        System.out.println("Table Name:"+pStmt.getTargetTable().toString());
        System.out.println("Columns:");
        TColumnDefinition column;
        for(int i=0;i<pStmt.getColumnList().size();i++){
            column = pStmt.getColumnList().getColumn(i);
            System.out.println("\tname:"+column.getColumnName().toString());
            System.out.println("\tdatetype:"+column.getDatatype().toString());
            if (column.getDefaultExpression() != null){
                System.out.println("\tdefault:"+column.getDefaultExpression().toString());
            }
            if (column.isNull()){
                System.out.println("\tnull: yes");
            }
            if (column.getConstraints() != null){
                System.out.println("\tinline constraints:");
                for(int j=0;j<column.getConstraints().size();j++){
                    printConstraint(column.getConstraints().getConstraint(j),false);
                }
            }
            System.out.println();
        }

        if(pStmt.getTableConstraints().size() > 0){
            System.out.println("\toutline constraints:");
            for(int i=0;i<pStmt.getTableConstraints().size();i++){
                printConstraint(pStmt.getTableConstraints().getConstraint(i), true);
                System.out.println();
            }
        }
    }

}
