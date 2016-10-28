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

import com.github.antego.bashmock.parser.command.CommandParsing;
import com.github.antego.bashmock.parser.command.PipelineParsing;
import com.github.antego.bashmock.parser.misc.*;
import com.github.antego.bashmock.parser.paramExpansion.ParameterExpansionParsing;
import com.github.antego.bashmock.parser.variable.VarParsing;

/**
 * The registry for all parsing related helper classes.
 * It gives access to the available parsing helpers. The instances are only created
 * one.
 * <br>
 * @author jansorg
 */
public final class Parsing {
    public static final FileParsing file = new FileParsing();
    public static final RedirectionParsing redirection = new RedirectionParsing();
    public static final CommandParsing command = new CommandParsing();
    public static final ShellCommandParsing shellCommand = new ShellCommandParsing();
    public static final ListParsing list = new ListParsing();
    public static final PipelineParsing pipeline = new PipelineParsing();
    public static final WordParsing word = new WordParsing();
    public static final VarParsing var = new VarParsing();
    public static final BraceExpansionParsing braceExpansionParsing = new BraceExpansionParsing();
    public static final ParameterExpansionParsing parameterExpansionParsing = new ParameterExpansionParsing();
    public static final ProcessSubstitutionParsing processSubstitutionParsing = new ProcessSubstitutionParsing();

    private Parsing() {
    }
}
