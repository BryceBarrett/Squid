/* 
 * Copyright 2016 James F. Bowring and CIRDLES.org.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.cirdles.squid.tasks.expressions.functions;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import java.util.List;
import org.cirdles.squid.exceptions.SquidException;
import org.cirdles.squid.shrimp.ShrimpFractionExpressionInterface;
import org.cirdles.squid.tasks.TaskInterface;
import static org.cirdles.squid.tasks.expressions.builtinExpressions.BuiltInExpressionsDataDictionary.LAMBDA_232_NAME;
import static org.cirdles.squid.tasks.expressions.builtinExpressions.BuiltInExpressionsDataDictionary.LAMBDA_238_NAME;
import org.cirdles.squid.tasks.expressions.constants.ConstantNode;
import org.cirdles.squid.tasks.expressions.expressionTrees.ExpressionTreeInterface;
import static org.cirdles.squid.tasks.expressions.expressionTrees.ExpressionTreeInterface.convertObjectArrayToDoubles;
import static org.cirdles.squid.tasks.expressions.builtinExpressions.BuiltInExpressionsDataDictionary.PRESENT_R238_235S_NAME;

/**
 *
 * @author James F. Bowring
 */
@XStreamAlias("Operation")
public class Rad8corPb7U5WithErr extends Function {

    //private static final long serialVersionUID = -6711265919551953531L;
    /**
     * This method combines Ludwig's Rad8corPb7U5 and Rad8corPb7U5Perr.
     *
     * Ludwig specifies Rad8corPb7U5: Returns the radiogenic 208-corrected
     * 207PbSTAR/235U ratio.
     *
     * Ludwig specifies Rad8corPb7U5Perr: Returns the %error of a 208-corrected
     * 207PbSTAR/235U.
     *
     * @see
     * https://raw.githubusercontent.com/CIRDLES/LudwigLibrary/master/vbaCode/isoplot3Basic/Pub.bas
     * @see
     * https://raw.githubusercontent.com/CIRDLES/LudwigLibrary/master/vbaCode/isoplot3Basic/UPb.bas
     */
    public Rad8corPb7U5WithErr() {

        name = "Rad8corPb7U5WithErr";
        argumentCount = 12;
        precedence = 4;
        rowCount = 1;
        colCount = 2;
        labelsForOutputValues = new String[][]{{"Age", "1\u03C3 abs"}};
        labelsForInputValues = new String[]{
            "Total 206/238, Total 206/238 1\u03C3 %,"
            + "8-corr 206*/238,"
            + "TotPb7U5,"
            + "Total 208/232, Total 208/232 1\u03C3 %, "
            + "Total 207/206, Total 207/206 1\u03C3 %,"
            + "Total 208/206, Total 208/206 1\u03C3 %,"
                + "sComm_76, sComm_86"};
    }

    /**
     *
     * Requires that children 0 - 9 are VariableNodes that evaluate to a double
     * array with column 1 representing the values for Total 206/238, Total
     * 206/238 1%Unct, 8-corr 206/238, TotPb7U5, Total 208/232, Total 208/232 1%Unct, Total 207/206, Total
     * 207/206 1%Unct, Total 208/206, Total 208/206 1%Unct with a row for each
     * member of shrimpFractions.
     *
     * @param childrenET list containing child 0-9
     * @param shrimpFractions a list of shrimpFractions
     * @param task
     * @return the double[1][2] array of 207PbSTAR/235U, 207PbSTAR/235UErr
     * @throws org.cirdles.squid.exceptions.SquidException
     */
    @Override
    public Object[][] eval(
            List<ExpressionTreeInterface> childrenET, List<ShrimpFractionExpressionInterface> shrimpFractions, TaskInterface task) throws SquidException {
        Object[][] retVal;
        try {
            double[] totPb6U8 = convertObjectArrayToDoubles(childrenET.get(0).eval(shrimpFractions, task)[0]);
            double[] totPb6U8per = convertObjectArrayToDoubles(childrenET.get(1).eval(shrimpFractions, task)[0]);
            double[] radPb6U8 = convertObjectArrayToDoubles(childrenET.get(2).eval(shrimpFractions, task)[0]);
            double[] totPb7U5 = convertObjectArrayToDoubles(childrenET.get(3).eval(shrimpFractions, task)[0]);
            double[] th2U8 = convertObjectArrayToDoubles(childrenET.get(4).eval(shrimpFractions, task)[0]);
            double[] th2U8per = convertObjectArrayToDoubles(childrenET.get(5).eval(shrimpFractions, task)[0]);
            double[] totPb76 = convertObjectArrayToDoubles(childrenET.get(6).eval(shrimpFractions, task)[0]);
            double[] totPb76per = convertObjectArrayToDoubles(childrenET.get(7).eval(shrimpFractions, task)[0]);
            double[] totPb86 = convertObjectArrayToDoubles(childrenET.get(8).eval(shrimpFractions, task)[0]);
            double[] totPb86per = convertObjectArrayToDoubles(childrenET.get(9).eval(shrimpFractions, task)[0]);
            double[] sComm_76 = convertObjectArrayToDoubles(childrenET.get(10).eval(shrimpFractions, task)[0]);
            double[] sComm_86 = convertObjectArrayToDoubles(childrenET.get(11).eval(shrimpFractions, task)[0]);
            
            double PRESENT_R238_235S = (Double) ((ConstantNode) task.getNamedParametersMap().get(PRESENT_R238_235S_NAME)).getValue();
            double lambda232 = (Double) ((ConstantNode) task.getNamedParametersMap().get(LAMBDA_232_NAME)).getValue();
            double lambda238 = (Double) ((ConstantNode) task.getNamedParametersMap().get(LAMBDA_238_NAME)).getValue();
            
            double[] rad8corPb7U5WithErr = org.cirdles.ludwig.squid25.PbUTh_2.rad8corPb7U5WithErr(totPb6U8[0],
                    totPb6U8per[0],
                    radPb6U8[0],
                    totPb7U5[0],
                    th2U8[0],
                    th2U8per[0],
                    totPb76[0],
                    totPb76per[0],
                    totPb86[0],
                    totPb86per[0],
                    sComm_76[0], sComm_86[0], 
                    PRESENT_R238_235S, lambda232, lambda238);
            retVal = new Object[][]{{rad8corPb7U5WithErr[0], rad8corPb7U5WithErr[1] * rad8corPb7U5WithErr[0] / 100.0}};
        } catch (ArithmeticException | IndexOutOfBoundsException | NullPointerException e) {
            retVal = new Object[][]{{0.0, 0.0}};
        }

        return retVal;
    }

    /**
     *
     * @param childrenET the value of childrenET
     * @return
     */
    @Override
    public String toStringMathML(List<ExpressionTreeInterface> childrenET) {

        StringBuilder retVal = new StringBuilder();
        retVal.append("<mrow>");
        retVal.append("<mi>").append(name).append("</mi>");
        retVal.append("<mfenced>");
        for (int i = 0; i < childrenET.size(); i++) {
            retVal.append(toStringAnotherExpression(childrenET.get(i))).append("&nbsp;\n");
        }

        retVal.append("</mfenced></mrow>\n");

        return retVal.toString();
    }

}
