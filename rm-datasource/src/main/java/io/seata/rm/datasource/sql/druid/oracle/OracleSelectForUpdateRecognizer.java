/*
 *  Copyright 1999-2019 Seata.io Group.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package io.seata.rm.datasource.sql.druid.oracle;

import com.alibaba.druid.sql.ast.SQLExpr;
import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.ast.expr.SQLBinaryOpExpr;
import com.alibaba.druid.sql.ast.expr.SQLVariantRefExpr;
import com.alibaba.druid.sql.ast.statement.SQLSelect;
import com.alibaba.druid.sql.ast.statement.SQLSelectQueryBlock;
import com.alibaba.druid.sql.ast.statement.SQLSelectStatement;
import com.alibaba.druid.sql.ast.statement.SQLTableSource;
import com.alibaba.druid.sql.ast.statement.SQLExprTableSource;
import com.alibaba.druid.sql.dialect.mysql.visitor.MySqlOutputVisitor;
import io.seata.rm.datasource.ParametersHolder;
import io.seata.rm.datasource.sql.SQLParsingException;
import io.seata.rm.datasource.sql.SQLSelectRecognizer;
import io.seata.rm.datasource.sql.SQLType;
import io.seata.rm.datasource.sql.druid.BaseRecognizer;

import java.util.ArrayList;
import java.util.List;

/**
 * The type oralceselect for update recognizer.
 *
 * @author ccg
 * @date 2019/3/25
 */

public class OracleSelectForUpdateRecognizer extends BaseRecognizer implements SQLSelectRecognizer {

    private final SQLSelectStatement ast;

    /**
     * Instantiates a new My sql select for update recognizer.
     *
     * @param originalSQL the original sql
     * @param ast         the ast
     */
    public OracleSelectForUpdateRecognizer(String originalSQL, SQLStatement ast) {
        super(originalSQL);
        this.ast = (SQLSelectStatement) ast;
    }

    @Override
    public SQLType getSQLType() {
        return SQLType.SELECT_FOR_UPDATE;
    }

    @Override
    public String getWhereCondition(final ParametersHolder parametersHolder, final ArrayList<List<Object>> paramAppenders) {
        SQLSelectQueryBlock selectQueryBlock = getSelect();
        SQLExpr where = selectQueryBlock.getWhere();
        if (where == null) {
            return "";
        }
        StringBuffer sb = new StringBuffer();
        MySqlOutputVisitor visitor = super.createMySqlOutputVisitor(parametersHolder, paramAppenders, sb);
        visitor.visit((SQLBinaryOpExpr) where);
        return sb.toString();
    }

    @Override
    public String getWhereCondition() {
        SQLSelectQueryBlock selectQueryBlock = getSelect();
        SQLExpr where = selectQueryBlock.getWhere();
        if (where == null) {
            return "";
        }
        StringBuffer sb = new StringBuffer();
        MySqlOutputVisitor visitor = new MySqlOutputVisitor(sb);
        visitor.visit((SQLBinaryOpExpr) where);
        return sb.toString();
    }

    private SQLSelectQueryBlock getSelect() {
        SQLSelect select = ast.getSelect();
        if (select == null) {
            throw new SQLParsingException("should never happen!");
        }
        SQLSelectQueryBlock selectQueryBlock = select.getQueryBlock();
        if (selectQueryBlock == null) {
            throw new SQLParsingException("should never happen!");
        }
        return selectQueryBlock;
    }

    @Override
    public String getTableAlias() {
        SQLSelectQueryBlock selectQueryBlock = getSelect();
        SQLTableSource tableSource = selectQueryBlock.getFrom();
        return tableSource.getAlias();
    }

    @Override
    public String getTableName() {
        SQLSelectQueryBlock selectQueryBlock = getSelect();
        SQLTableSource tableSource = selectQueryBlock.getFrom();
        StringBuffer sb = new StringBuffer();
        MySqlOutputVisitor visitor = new MySqlOutputVisitor(sb) {

            @Override
            public boolean visit(SQLExprTableSource x) {
                printTableSourceExpr(x.getExpr());
                return false;
            }
        };
        visitor.visit((SQLExprTableSource) tableSource);
        return sb.toString();
    }

}
