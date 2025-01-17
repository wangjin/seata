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
package io.seata.rm.datasource.sql.druid;

import com.alibaba.druid.sql.ast.expr.SQLVariantRefExpr;
import com.alibaba.druid.sql.dialect.mysql.visitor.MySqlOutputVisitor;
import io.seata.rm.datasource.ParametersHolder;
import io.seata.rm.datasource.sql.SQLRecognizer;

import java.util.ArrayList;
import java.util.List;

/**
 * The type Base recognizer.
 *
 * @author sharajava
 */
public abstract class BaseRecognizer implements SQLRecognizer {

    /**
     * The type V marker.
     */
    public static class VMarker {
        @Override
        public String toString() {
            return "?";
        }

    }

    /**
     * The Original sql.
     */
    protected String originalSQL;

    /**
     * Instantiates a new Base recognizer.
     *
     * @param originalSQL the original sql
     */
    public BaseRecognizer(String originalSQL) {
        this.originalSQL = originalSQL;

    }

    @Override
    public String getOriginalSQL() {
        return originalSQL;
    }

    public MySqlOutputVisitor createMySqlOutputVisitor(final ParametersHolder parametersHolder, final ArrayList<List<Object>> paramAppenders, final StringBuffer sb) {
        MySqlOutputVisitor visitor = new MySqlOutputVisitor(sb) {

            @Override
            public boolean visit(SQLVariantRefExpr x) {
                if ("?".equals(x.getName())) {
                    ArrayList<Object> oneParamValues = parametersHolder.getParameters()[x.getIndex()];
                    if (paramAppenders.size() == 0) {
                        oneParamValues.stream().forEach(t -> paramAppenders.add(new ArrayList<>()));
                    }
                    for (int i = 0; i < oneParamValues.size(); i++) {
                        paramAppenders.get(i).add(oneParamValues.get(i));
                    }

                }
                return super.visit(x);
            }
        };
        return visitor;
    }
}
