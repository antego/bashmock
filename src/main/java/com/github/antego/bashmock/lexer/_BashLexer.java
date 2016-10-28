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

package com.github.antego.bashmock.lexer;

import com.github.antego.bashmock.BashVersion;
import com.github.antego.bashmock.util.IntStack;

final class _BashLexer extends _BashLexerBase implements BashLexerDef {
    private final IntStack lastStates = new IntStack(25);
    //Help data to parse (nested) strings.
    private final StringLexingstate string = new StringLexingstate();
    //parameter expansion parsing state
    private boolean paramExpansionHash = false;
    private boolean paramExpansionWord = false;
    private boolean paramExpansionOther = false;
    private int openParenths = 0;
    private boolean isBash4 = false;
    //True if the parser is in the case body. Necessary for proper lexing of the IN keyword
    private boolean inCaseBody = false;
    //conditional expressions
    private boolean emptyConditionalCommand = false;
    private final HeredocLexingState heredocState = new HeredocLexingState();

    @Override
    public HeredocLexingState heredocState() {
        return heredocState;
    }

    _BashLexer(BashVersion version, java.io.Reader in) {
        super(in);

        this.isBash4 = BashVersion.Bash_v4.equals(version);
    }

    @Override
    public boolean isEmptyConditionalCommand() {
        return emptyConditionalCommand;
    }

    @Override
    public void setEmptyConditionalCommand(boolean emptyConditionalCommand) {
        this.emptyConditionalCommand = emptyConditionalCommand;
    }

    @Override
    public StringLexingstate stringParsingState() {
        return string;
    }

    @Override
    public boolean isInCaseBody() {
        return inCaseBody;
    }

    @Override
    public void setInCaseBody(boolean inCaseBody) {
        this.inCaseBody = inCaseBody;
    }

    @Override
    public boolean isBash4() {
        return isBash4;
    }

    /**
     * Goes to the given state and stores the previous state on the stack of states.
     * This makes it possible to have several levels of lexing, e.g. for $(( 1+ $(echo 3) )).
     */
    public void goToState(int newState) {
        lastStates.push(yystate());
        yybegin(newState);
    }

    /**
     * Goes back to the previous state of the lexer. If there
     * is no previous state then YYINITIAL, the initial state, is chosen.
     */
    public void backToPreviousState() {
        // pop() will throw an exception if empty
        yybegin(lastStates.pop());
    }

    @Override
    public void popStates(int lastStateToPop) {
        if (yystate() == lastStateToPop) {
            backToPreviousState();
            return;
        }

        while (isInState(lastStateToPop)) {
            boolean finished = (yystate() == lastStateToPop);
            backToPreviousState();

            if (finished) {
                break;
            }
        }
    }

    @Override
    public boolean isInState(int state) {
        return (state == 0 && lastStates.empty()) || lastStates.contains(state);
    }

    @Override
    public int openParenthesisCount() {
        return openParenths;
    }

    @Override
    public void incOpenParenthesisCount() {
        openParenths++;
    }

    @Override
    public void decOpenParenthesisCount() {
        openParenths--;
    }

    @Override
    public boolean isParamExpansionWord() {
        return paramExpansionWord;
    }

    @Override
    public void setParamExpansionWord(boolean paremeterExpansionWord) {
        this.paramExpansionWord = paremeterExpansionWord;
    }

    @Override
    public boolean isParamExpansionOther() {
        return paramExpansionOther;
    }

    @Override
    public void setParamExpansionOther(boolean paremeterExpansionOther) {
        this.paramExpansionOther = paremeterExpansionOther;
    }

    @Override
    public boolean isParamExpansionHash() {
        return paramExpansionHash;
    }

    @Override
    public void setParamExpansionHash(boolean paremeterExpansionHash) {
        this.paramExpansionHash = paremeterExpansionHash;
    }
}
