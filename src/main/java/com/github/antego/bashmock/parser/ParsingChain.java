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

package com.github.antego.bashmock.parser;

import java.util.LinkedList;
import java.util.List;

/**
 * A parsing chain contains a list of parsing functions which are all called after each other.
 * Basically this is a delegating ParsingFunction  which delegates to the first
 * parsing function which understands the token sequence. If no such function can be found
 * false is returned.
 * <br>
 * @author jansorg
 */
public abstract class ParsingChain implements ParsingFunction {
    private final List<ParsingFunction> parsingFunctions = new LinkedList<ParsingFunction>();

    protected final void addParsingFunction(ParsingFunction f) {
        parsingFunctions.add(f);
    }

    public boolean isValid(BashPsiBuilder builder) {
        if (builder.eof()) return false;

        for (ParsingFunction f : parsingFunctions) {
            if (f.isValid(builder))
                return true;
        }

        return false;
    }

    public boolean parse(BashPsiBuilder builder) {
        if (builder.eof()) return false;

        for (ParsingFunction f : parsingFunctions) {
            if (f.isValid(builder)) {
                return f.parse(builder);
            }
        }

        return false;
    }
}
