import com.microsoft.z3.*;

import java.util.*;
import java.util.stream.Collectors;
import java.io.File;
import java.io.FileNotFoundException;


public class ConflictSetRetriever {
    private String documentPath;
    private List<String> document;
    private Context ctx;
    private List<BoolExpr> inVars;
    private List<BoolExpr> outVars;
    private List<BoolExpr> comps;
    private List<BoolExpr> allGates;
    private List<BoolExpr> faultAssumptions;
    private List<BoolExpr> obs;

    public ConflictSetRetriever(String documentPath) {
        this.documentPath = documentPath;
        HashMap<String, String> cfg = new HashMap<>();
        this.ctx = new Context(cfg);

        try {
            this.document = openDocument();
            validateDocument();
            this.inVars = extractInObs();
            this.outVars = extractOutObs();
            List<List<BoolExpr>> compsGates = extractGates();
            this.comps = compsGates.get(0);
            this.allGates = compsGates.get(1);
            this.faultAssumptions = makeFaultAssumptions();
            this.obs = extractObs();
        } catch (FileNotFoundException e) {
            System.err.println("File not found: " + documentPath);
            e.printStackTrace();
        } catch (InputMismatchException e) { // For invalid circuits
            System.err.println("Input circuit is invalid: " + e.getMessage());
        } catch (AssertionError e) {
            System.err.println("Assertion failed: " + e.getMessage());
        }
    }

    public String getDocumentPath() {
        return this.documentPath;
    }

    public List<String> getDocument() {
        return this.document;
    }

    public Context getCtx() {
        return this.ctx;
    }

    public List<BoolExpr> getInVars() {
        return this.inVars;
    }

    public List<BoolExpr> getOutVars() {
        return this.outVars;
    }

    public List<BoolExpr> getComps() {
        return this.comps;
    }

    public List<BoolExpr> getAllGates() {
        return this.allGates;
    }

    public List<BoolExpr> getFaultAssumptions() {
        return this.faultAssumptions;
    }

    public List<BoolExpr> getObs() {
        return this.obs;
    }

    /**
     * Opens the document file at the path given by documentPath and returns
     * its contents as a list of strings, excluding blank lines.
     * @return The contents of the document as a list of strings.
     * @throws FileNotFoundException If the specified file does not exist.
     */
    private List<String> openDocument() throws FileNotFoundException{
        Scanner scanner = new Scanner(new File(this.documentPath));
        List<String> doc = new ArrayList<>();
        
        while (scanner.hasNext()) {
            String line = scanner.nextLine().trim();
            if (!line.isEmpty()) { // Exclude blank lines
                doc.add(line);
            }
        }
        scanner.close();
        return doc;
    }

    /**
     * Validates the document by checking for the presence of the required sections.
     * @throws InputMismatchException If the document does not contain the required sections.
     */
    private void validateDocument() throws InputMismatchException {
        // Check for components
        boolean comps = this.document.contains("COMPONENTS:") && this.document.contains("ENDCOMPONENTS");
        if (!comps) {
            throw new InputMismatchException("Missing COMPONENTS section");
        }

        // Check for system behaviour
        boolean behaviour = this.document.contains("BEHAVIOUR:") && this.document.contains("ENDBEHAVIOUR");
        if (!behaviour) {
            throw new InputMismatchException("Missing BEHAVIOUR section");
        }

        // Check for in-observations (aka basic event)
        boolean inObs = this.document.contains("OBSERVATIONS:") && this.document.contains("ENDOBSERVATIONS");
        if (!inObs) {
            throw new InputMismatchException("Missing OBSERVATION section");
        }

        // Check for out-observations (system output)
        boolean outObs = this.document.contains("OUTOBSERVATIONS:") && this.document.contains("ENDOUTOBSERVATIONS");
        if (!outObs) {
            throw new InputMismatchException("Missing OUTOBSERVATION section");
        }

        // Check that each component has exactly one IN1 and one IN2 connection
        int firstCompInd = this.document.indexOf("COMPONENTS:") + 1;
        int lastCompInd = this.document.indexOf("ENDCOMPONENTS");
        List<String> components = this.document.subList(firstCompInd, lastCompInd);

        Map<String, Map<String, Integer>> inCounts = new HashMap<>();
        for (String comp : components) {
            Map<String, Integer> innerMap = new HashMap<>();
            String be = comp.substring(comp.indexOf("(") + 1, comp.indexOf(")"));
            int in1Count = 0;
            int in2Count = 0;
            for (String line : this.document) {
                if (line.contains("IN1(" + be + ")=")) { in1Count++;}
                if (line.contains("IN2(" + be + ")=")) { in2Count++;}
            }

            innerMap.put("IN1", in1Count);
            innerMap.put("IN2", in2Count);
            inCounts.put(comp, innerMap);
        }

        List<String> errors = new ArrayList<>();
        for (Map.Entry<String, Map<String, Integer>> count : inCounts.entrySet()) {
            String comp = count.getKey();
            Integer in1Count = count.getValue().get("IN1");
            Integer in2Count = count.getValue().get("IN2");
            if (in1Count != 1 || in2Count != 1) {
                errors.add("- component " + comp + " has " + in1Count + " IN1 connections and "
                + in2Count + "IN2 connections.");
            }
        }

        if (!errors.isEmpty()) {
            throw new InputMismatchException("Invalid component connections:\n" + String.join("\n", errors));
        }
    }

    /**
     * Extracts the input observations from the document.
     * @return A list of boolean expressions representing the input observations.
     * @throws InputMismatchException If no observations are found in the document.
     * @throws AssertionError If there are no input observations.
     */
    private List<BoolExpr> extractInObs() throws InputMismatchException, AssertionError {
        // Create list of only observations
        int firstObsInd = this.document.indexOf("OBSERVATIONS:") + 1;
        int lastComObs = this.document.indexOf("ENDOBSERVATIONS");
        List<String> inObs = this.document.subList(firstObsInd, lastComObs);
        if (inObs.isEmpty()) {
            throw new InputMismatchException("No observations found.");
        }

        // Create list of boolean expressions of observations
        List<BoolExpr> inObsBool = new ArrayList<>();
        for (String inOb : inObs) {
            String inObName = inOb.split("=")[0];
            inObsBool.add(this.ctx.mkBoolConst(inObName));
        }
        assert !inObsBool.isEmpty(): "There must be at least one input observation.";
        return inObsBool;
    }

    /**
     * Extracts the output observations from the document.
     * @return A list of boolean expressions representing the output observations.
     * @throws InputMismatchException If no output observations are found in the document.
     * @throws AssertionError If there are no output observations.
     */
    private List<BoolExpr> extractOutObs() throws InputMismatchException, AssertionError {
        // Create list of only output observations
        int firstObsInd = this.document.indexOf("OUTOBSERVATIONS:") + 1;
        int lastObsInd = this.document.indexOf("ENDOUTOBSERVATIONS");
        List<String> outObs = this.document.subList(firstObsInd, lastObsInd);
        if (outObs.isEmpty()) {
            throw new InputMismatchException("No end observations found.");
        }

        // Create list of boolean expressions of output observations
        List<BoolExpr> outObsBool = new ArrayList<>();
        for (String outOb : outObs) {
            String outObName = outOb.split("=")[0];
            outObsBool.add(this.ctx.mkBoolConst(outObName));
        }
        assert !outObsBool.isEmpty(): "There must be at least one output observation.";
        return outObsBool;
    }

    /**
     * Extracts the component and gate boolean expressions from the document.
     * @return A list of lists of boolean expressions, where the first list contains the component
     *          boolean expressions and the second list contains the gate boolean expressions.
     * @throws InputMismatchException If no observations are found in the document.
     */
    private List<List<BoolExpr>> extractGates() throws InputMismatchException {
        // Create list of only components
        int firstCompInd = this.document.indexOf("COMPONENTS:") + 1;
        int lastCompInd = this.document.indexOf("ENDCOMPONENTS");
        List<String> compsString = this.document.subList(firstCompInd, lastCompInd);
        if (compsString.isEmpty()) {
            throw new InputMismatchException("No observations found.");
        }

        // Create list of boolean expressions of components and gates
        List<BoolExpr> compsBool = new ArrayList<>(); // components
        List<BoolExpr> gatesBool = new ArrayList<>(); // gates
        for (String comp : compsString) {
            String compName = comp.substring(comp.indexOf("(") + 1, comp.indexOf(")"));
            if (compName.isEmpty()) {
                throw new InputMismatchException("Error reading components.");
            }
            compsBool.add(ctx.mkBoolConst(compName + "Gate"));
            gatesBool.add(ctx.mkBoolConst(compName));
        }
        List<List<BoolExpr>> result = new ArrayList<>();
        result.add(compsBool);
        result.add(gatesBool);
        return result;
    }

    /**
     * Returns a boolean expression representing the faulted condition of a gate.
     * The faulted condition is true if the gate's output is not equal to the
     * logical expression of the gate's input or if the fault flag is true.
     * @param gateOut The output of the gate.
     * @param logicExpr The logical expression of the gate's input.
     * @param faultFlag The fault flag of the gate.
     * @return A boolean expression representing the faulted condition of the gate.
     */
    private BoolExpr faulted(BoolExpr gateOut, BoolExpr logicExpr, BoolExpr faultFlag) {
        BoolExpr eq = this.ctx.mkEq(gateOut, logicExpr);
        return this.ctx.mkOr(faultFlag, eq);
    }

    /**
     * Finds the correspondinging gate type of a component given its output and two inputs.
     * @param compOut The output of the component.
     * @param inputs The two inputs of the component.
     * @return A boolean expression representing the gate type of the component.
     * @throws InputMismatchException If the component is not found in the document or if the
     *         component's gate type is not supported.
     */
    private BoolExpr findCorrespondingGateType(BoolExpr compOut, BoolExpr[] inputs) throws InputMismatchException {
        assert inputs.length == 2: "There must be exactly two inputs.";

        // Create list of only components
        int firstCompInd = this.document.indexOf("COMPONENTS:") + 1;
        int lastCompInd = this.document.indexOf("ENDCOMPONENTS");
        List<String> comps = this.document.subList(firstCompInd, lastCompInd);
        if (comps.isEmpty()) {
            throw new InputMismatchException("No observations found.");
        }

        // Find the component and return its corresponding gate type
        for (String comp : comps) {
            if (!comp.contains(compOut.toString())) {
                continue;
            }

            String[] compSplit = comp.split("\\(");
            switch (compSplit[0]) {
                case "ANDG":
                    return this.ctx.mkAnd(inputs[0], inputs[1]);
                case "ORG":
                    return this.ctx.mkOr(inputs[0], inputs[1]);
                case "XORG":
                    return this.ctx.mkXor(inputs[0], inputs[1]);
            }
        }
        throw new InputMismatchException("Error reading components.");
    }

    /**
     * Finds the two inputs of a component given its output.
     * The inputs are found by searching through the system behaviour and
     * observations in the document.
     * @param compOut The output of the component.
     * @return An array of two boolean expressions representing the two inputs of the component.
     * @throws InputMismatchException If no two inputs are found for the component.
     */
    private BoolExpr[] findInputs(BoolExpr compOut) throws InputMismatchException {
        BoolExpr input1 = null;
        BoolExpr input2 = null;
        String comp = compOut.toString();

        // Create list of only system behaviour
        int firstBehaviourInd = this.document.indexOf("BEHAVIOUR:") + 1;
        int lastBehaviourInd = this.document.indexOf("ENDBEHAVIOUR");
        List<String> behaviours = this.document.subList(firstBehaviourInd, lastBehaviourInd);

        // Create list of only components
        int firstCompInd = this.document.indexOf("COMPONENTS:") + 1;
        int lastCompInd = this.document.indexOf("ENDCOMPONENTS");
        List<String> compsString = this.document.subList(firstCompInd, lastCompInd);
        compsString = compsString.stream().map(s -> s.substring(s.indexOf("("), s.length())).collect(Collectors.toList());
        
        // Find inputs in system behaviour
        for (int i = 0; i < behaviours.size(); i++) {
            if (behaviours.get(i).contains("IN1(" + comp + ")=")) {
                String c = behaviours.get(i).split("=")[1].replace("OUT", "");
                input1 = this.comps.get(compsString.indexOf(c));
            } else if (behaviours.get(i).contains("IN2(" + comp + ")=")) {
                String c = behaviours.get(i).split("=")[1].replace("OUT", "");
                input2 = this.comps.get(compsString.indexOf(c));
            }
        }
        if (input1 != null && input2 != null) {
            return new BoolExpr[] {input1, input2};
        }

        // Create list of only in-observations
        int firstInVarInd = this.document.indexOf("OBSERVATIONS:") + 1;
        int lastInVarInd = this.document.indexOf("ENDOBSERVATIONS");
        List<String> inVarsString = this.document.subList(firstInVarInd, lastInVarInd);

        // Find inputs in in-observations
        for (int i = 0; i < inVarsString.size(); i++) {
            if (inVarsString.get(i).contains("IN1(" + comp + ")=1") || inVarsString.get(i).contains("IN1(" + comp + ")=0")) {
                input1 = this.inVars.get(i);
            } else if (inVarsString.get(i).contains("IN2(" + comp + ")=1") || inVarsString.get(i).contains("IN2(" + comp + ")=0")) {
                input2 = this.inVars.get(i);
            }
        }
        if (input1 != null && input2 != null) {
            return new BoolExpr[] {input1, input2};
        }
        throw new InputMismatchException("No two inputs found for this component.");
    }

    /**
     * Makes a list of fault assumptions based on the components and gates in the document.
     * A fault assumption is a boolean expression representing the faulted condition of a gate.
     * It is true if the gate's output is not equal to the logical expression of the gate's input or if the fault flag is true.
     * @return A list of boolean expressions representing the faulted conditions of the gates.
     * @throws AssertionError If the amount of components is not equal to the amount of gates or if there is no fault assumption.
     */
    private List<BoolExpr> makeFaultAssumptions() throws AssertionError{
        assert this.comps.size() == this.allGates.size(): "Amount of components is not equal to the amount of gates.";

        List<BoolExpr> faultAssumptions = new ArrayList<>();
        for (int i = 0; i < this.comps.size(); i++) {
            BoolExpr compIn = this.comps.get(i);
            BoolExpr compOut = this.allGates.get(i);
            BoolExpr[] inputs = findInputs(compOut);
            BoolExpr gate = findCorrespondingGateType(compOut, inputs);
            faultAssumptions.add(faulted(compIn, gate, compOut));
        }
        assert !faultAssumptions.isEmpty(): "There must be at least one fault assumption.";
        return faultAssumptions;
    }

    /**
     * Extracts the component and gate boolean expressions from the document.
     * @return A list of boolean expressions, where the first list contains the component
     *          boolean expressions and the second list contains the gate boolean expressions.
     * @throws InputMismatchException If no observations are found in the document.
     * @throws AssertionError If not all observations have been processed.
     */
    private List<BoolExpr> extractObs() throws InputMismatchException, AssertionError {
        // Create list of only observations
        int firstObsInd = this.document.indexOf("OBSERVATIONS:") + 1;
        int lastObsInd = this.document.indexOf("ENDOBSERVATIONS");
        List<String> obsString = this.document.subList(firstObsInd, lastObsInd);
        if (obsString.isEmpty()) {
            throw new InputMismatchException("No observations found.");
        }

        // Setting all in-observations equal to their value (0 or 1)
        List<BoolExpr> allObs = new ArrayList<>();
        for (int i = 0; i < obsString.size(); i++) {
            String compObservation = obsString.get(i).split("=")[1];
            if (compObservation.equals("1")) {
                allObs.add(this.ctx.mkEq(this.inVars.get(i), this.ctx.mkTrue()));
            } else if (compObservation.equals("0")) {
                allObs.add(this.ctx.mkEq(this.inVars.get(i), this.ctx.mkFalse()));
            } else {
                throw new InputMismatchException("No proper observations found.");
            }
        }

        // Create list of only out-observations
        int firstOutObsInd = this.document.indexOf("OUTOBSERVATIONS:") + 1;
        int lastOutObsInd = this.document.indexOf("ENDOUTOBSERVATIONS");
        List<String> outObs = this.document.subList(firstOutObsInd, lastOutObsInd);
        if (outObs.isEmpty()) {
            throw new InputMismatchException("No out observations found.");
        }

        // Setting all out-observations equal to their value (0 or 1)
        for (String outOb : outObs) {
            String[] obsSplit = outOb.split("=");
            String compName = obsSplit[0].replace("OUT(", "").replace(")", "");
            for (int i = 0; i < this.allGates.size(); i++) {
                if (compName.equals(this.allGates.get(i).toString())) {
                    if (obsSplit[1].equals("1")) {
                        allObs.add(this.ctx.mkEq(this.comps.get(i), this.ctx.mkTrue()));
                    } else if (obsSplit[1].equals("0")) {
                        allObs.add(this.ctx.mkEq(this.comps.get(i), this.ctx.mkFalse()));
                    } else {
                        throw new InputMismatchException("No proper observations found.");
                    }
                }
            }
        }
        assert allObs.size() == (inVars.size() + outObs.size()): "Not all observations have been processed.";
        assert !allObs.isEmpty(): "There must be at least one observation.";
        return allObs;
    }

    /**
     * Generates the powerset of a given generic set.
     * The powerset of a set is the set of all possible subsets of the original set.
     * @param originalSet The set for which the powerset should be generated.
     * @return The powerset of the original set.
     * Borrowed from Stackoverflow: https://stackoverflow.com/questions/1670862/obtaining-a-powerset-of-a-set-in-java
     */
    private static <T> Set<Set<T>> powerset(Set<T> originalSet) {
        assert !originalSet.isEmpty(): "The original set must not be empty.";
        Set<Set<T>> sets = new HashSet<Set<T>>();
        List<T> list = new ArrayList<T>(originalSet);
        T head = list.get(0);
        Set<T> rest = new HashSet<T>(list.subList(1, list.size()));
        
        for (Set<T> set : powerset(rest)) {
            Set<T> newSet = new HashSet<T>();
            newSet.add(head);
            newSet.addAll(set);
            sets.add(newSet);
            sets.add(set);
        }    
        return sets;
    }
    /**
     * Retrieves the conflict sets for the given gates and observations.
     * @return A list of lists of strings, where each inner list represents a conflict set.
     */
    public List<List<String>> retrieveConflictSets() {
        Set<Set<BoolExpr>> conflictSets = new HashSet<>();
        Set<Set<BoolExpr>> powerset = powerset(new HashSet<BoolExpr>(this.allGates));
        powerset.remove(new HashSet<BoolExpr>()); // Remove empty set

        // Initialize solver
        Solver solver = this.ctx.mkSolver();    
        
        // Loop through all combinations of gates
        for (Set<BoolExpr> healthy : powerset) {
            // Add observations and fault assumptions to solver
            for (BoolExpr o : this.obs) {
                solver.add(o);
            }
            for (BoolExpr fa : this.faultAssumptions) {
                solver.add(fa);
            }

            // Assume gates in the combination are healthy
            for (BoolExpr gate : this.allGates) {
                if (healthy.contains(gate)) {
                    solver.add(this.ctx.mkNot(gate));
                } else {
                    solver.add(gate);
                }
            }

            if (solver.check() == Status.UNSATISFIABLE) {
                conflictSets.add(healthy);
            }
            solver.reset();            
        }
        
        // Filter for minimal conflict sets
        List<List<String>> minimalConflicts = new ArrayList<>();
        for (Set<BoolExpr> conflictSet : conflictSets) {
            boolean isMinimal = true;
            for (Set<BoolExpr> otherConflictSet : conflictSets) {
                if (!conflictSet.equals(otherConflictSet) && conflictSet.containsAll(otherConflictSet)) {
                    isMinimal = false;
                    break;
                }
            }
            if (isMinimal) {
                minimalConflicts.add(conflictSet.stream().map(BoolExpr::toString).collect(Collectors.toList()));
            }
        }
        return minimalConflicts;
    }
}
