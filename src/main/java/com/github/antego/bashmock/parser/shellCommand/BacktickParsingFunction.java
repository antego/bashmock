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

package com.github.antego.bashmock.parser.shellCommand;

import com.github.antego.bashmock.lexer.BashTokenTypes;
import com.github.antego.bashmock.parser.BashElementTypes;
import com.github.antego.bashmock.parser.BashPsiBuilder;
import com.github.antego.bashmock.parser.Parsing;
import com.github.antego.bashmock.parser.ParsingFunction;
import com.github.antego.bashmock.parser.util.ParserUtil;
import com.intellij.lang.PsiBuilder;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.psi.tree.IElementType;

/**
 * Parsing function for backtic / backquote calls.
 * <br>
 * @author jansorg
 */
public class BacktickParsingFunction implements ParsingFunction {
    private static final Logger log = Logger.getInstance("#bash.BackquoteParsing");

    public boolean isValid(BashPsiBuilder builder) {
        return !builder.getBackquoteData().isInBackquote() && builder.getTokenType() == BashTokenTypes.BACKQUOTE;
    }

    public boolean parse(BashPsiBuilder builder) {
        /*
          backquote: '`' compound_list '`'
         */

        final PsiBuilder.Marker backquote = builder.mark();
        builder.advanceLexer(); //after the initial backquote

        builder.getBackquoteData().enterBackquote();
        try {
            final boolean empty = builder.getTokenType() == BashTokenTypes.BACKQUOTE;

            //parse compound list
            if (!empty) {
                if (!Parsing.list.parseCompoundList(builder, true, false)) {
                    ParserUtil.error(backquote, "parser.shell.expectedCommands");
                    return false;
                }
            }

            //get and check end token
            final IElementType lastToken = ParserUtil.getTokenAndAdvance(builder);
            if (lastToken != BashTokenTypes.BACKQUOTE) {
                ParserUtil.error(backquote, "parser.unexpected.token");
                return false;
            }

            backquote.done(BashElementTypes.BACKQUOTE_COMMAND);
            return true;
        } finally {
            builder.getBackquoteData().leaveBackquote();
        }
    }
}
