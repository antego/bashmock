/*
 * Copyright (c) Joachim Ansorg, mail@ansorg-it.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.antego.bashmock.parser.arithmetic;

import com.github.antego.bashmock.parser.BashPsiBuilder;
import com.github.antego.bashmock.parser.util.ParserUtil;
import com.intellij.lang.PsiBuilder;

/**
 * Parsing of prefix increment operators.
 * <br>
 * @author jansorg
 */
class PreIncrementExpr implements ArithmeticParsingFunction {
    private ArithmeticParsingFunction next;

    PreIncrementExpr(ArithmeticParsingFunction next) {
        this.next = next;
    }

    public boolean isValid(BashPsiBuilder builder) {
        return arithmeticPreOps.contains(builder.getTokenType()) || next.isValid(builder);
    }

    public boolean parse(BashPsiBuilder builder) {
        PsiBuilder.Marker marker = builder.mark();
        boolean mark = ParserUtil.conditionalRead(builder, arithmeticPreOps);

        boolean ok = next.parse(builder);

        if (mark) {
            marker.done(ARITH_PRE_INC_ELEMENT);
        } else {
            marker.drop();
        }

        return ok;
    }
}