from z3 import *
from itertools import chain, combinations
from typing import Tuple, List, Callable
import re


class ConflictSetRetriever:
    """
    Class that handles reads in a file and finds the conflict sets.
    """
    def __init__(self, document_path):
        """
        Opens file and handles logic to read the system description

        :param document_path: path to the circuit file.
        """
        self.document_path = document_path
        self.document = self.open_document()
        self.validate_file()

        self.in_variables = self.extract_in_observations()
        self.out_variables = self.extract_out_observations()
        self.comps, self.all_gates = self.extract_gates()
        self.fault_assumptions = self.make_fault_assumptions()
        self.observations = self.extract_observations()


    def open_document(self) -> str:
        """
        Opens document and returns as a string

        :return: the document as string
        """
        with open(self.document_path, "r") as file:
            return file.read()


    def validate_file(self):
        """
        Checks if the circuit file has a correct format, throws error if not.
        """
        comp_section = re.search(r"COMPONENTS:\s*(.*?)\s*ENDCOMPONENTS", self.document, re.DOTALL)
        if not comp_section:
            raise Exception("Missing COMPONENTS section")

        behaviour_section = re.search(r"BEHAVIOUR:\s*(.*?)\s*ENDBEHAVIOUR", self.document, re.DOTALL)
        if not behaviour_section:
            raise Exception("Missing BEHAVIOUR section")

        observation_section = re.search(r"OBSERVATIONS:\s*(.*?)\s*ENDOBSERVATIONS", self.document, re.DOTALL)
        if not observation_section:
            raise Exception("Missing OBSERVATION section")

        out_observation_section = re.search(r"OUTOBSERVATIONS:\s*(.*?)\s*ENDOUTOBSERVATIONS", self.document, re.DOTALL)
        if not out_observation_section:
            raise Exception("Missing OUTOBSERVATION section")

        components = re.findall(r"\((.*?)\)", comp_section.group(1))

        # Count IN1 and IN2 for each component
        in_counts = {comp: {"IN1": 0, "IN2": 0} for comp in components}

        for comp in components:
            in_counts[comp]["IN1"] = len(
                re.findall(rf"\bIN1\({re.escape(comp)}\)\s*=", self.document))
            in_counts[comp]["IN2"] = len(
                re.findall(rf"\bIN2\({re.escape(comp)}\)\s*=", self.document))

        errors = []
        for comp, counts in in_counts.items():
            if counts["IN1"] != 1 or counts["IN2"] != 1:
                errors.append(
                    f"- component {comp} has {counts['IN1']} IN1 connections and "
                    f"{counts['IN2']} IN2 connections.")

        if errors:
            raise ValueError("Invalid component connections:\n" + "\n".join(errors))


    def extract_in_observations(self) -> List[z3.z3.BoolRef]:
        """
        Reads in the in-observations and stores them as list of Z3 Bools.

        :return: list of Z3 Bools.
        """
        match = re.search(r"\s*OBSERVATIONS:(.*?)ENDOBSERVATIONS", self.document, re.DOTALL)
        if not match:
            raise Exception("No observations found.")
        else:
            observations_string = match.group(1)
            observations = observations_string.split("\n")
            observations = list(filter(len, observations))  # remove empty strings

            obs_names = []
            for observation in observations:
                obs_name = observation.split("=")[0]
                obs_names.append(Bools(obs_name)[0])  # only store e.g. IN1(X1)
            return obs_names


    def extract_out_observations(self) -> List[z3.z3.BoolRef]:
        """
        Reads in the out-observations and stores them as list of Z3 Bools.

        :return: list of Z3 Bools.
        """
        match = re.search(r"\s*OUTOBSERVATIONS:(.*?)ENDOUTOBSERVATIONS", self.document, re.DOTALL)
        if not match:
            raise Exception("No observations found.")
        else:
            observations_string = match.group(1)
            observations = observations_string.split("\n")
            observations = list(filter(len, observations))  # remove empty strings

            obs_names = []
            for observation in observations:
                obs_name = observation.split("=")[0]
                obs_names.append(Bools(obs_name)[0])  # only store e.g. OUT(X2)
                
            return obs_names


    def extract_gates(self) -> Tuple[List[z3.z3.BoolRef], List[z3.z3.BoolRef]]:
        """
        Reads in the gate names. Used for both the gates and gate output.
        Discards gate type.

        :return: list of Z3 Bools of gate names and gate output names
        """

        match = re.search(r"\s*COMPONENTS:(.*?)ENDCOMPONENTS", self.document, re.DOTALL)
        if not match:
            raise Exception("No components found.")
        else:
            components_string = match.group(1)
            components = components_string.split("\n")
            components = list(filter(len, components))  # remove empty strings

            comp_names = []
            comp_out_names = []
            for component in components:
                comp_name = re.search(r"\((.+)\)", component)
                if not comp_name:
                    raise Exception("Error reading components.")
                else:
                    comp_names.append(Bools(comp_name.group(1) + "_gate")[0])
                    comp_out_names.append(Bools(comp_name.group(1))[0])
            
            return comp_names, comp_out_names  # e.g. X1, X1_gate


    @staticmethod
    def faulted(gate_out, logic_expr, fault_flag) -> z3.z3.BoolRef:
        """
        Encode the faulty behaviour of a gate
        (helper function for making fault assumptions)

        :param gate_out: gate output
        :param logic_expr: boolean logic expression
        :param fault_flag: fault flag
        :return: Or: faulty behaviour
        """
        return Or(fault_flag, gate_out == logic_expr)


    def find_corresponding_gate_type(self, comp_out) -> Callable:
        """
        Finds the gate type for a certain gate.
        (helper function for making fault assumptions)

        :param comp_out: the gate to find the type of
        :return: the gate type as Z3 function
        """
        match = re.search(r"\s*COMPONENTS:(.*?)ENDCOMPONENTS", self.document, re.DOTALL)
        if not match:
            raise Exception("No components found.")
        else:
            components_string = match.group(1)
            components = components_string.split("\n")
            components = list(filter(len, components))  # remove empty strings
            for component in components:
                comp_regex = re.search(r"(ANDG|ORG|XORG)\((.+)\)", component)
                if not comp_regex:
                    raise Exception("Error reading components.")
                else:
                    if comp_regex.group(2) == str(comp_out):
                        gate = comp_regex.group(1)
                        if gate == "ANDG":
                            return And
                        elif gate == "ORG":
                            return Or
                        elif gate == "XORG":
                            return Xor


    def find_inputs(self, comp_out) -> Tuple[Tuple[int, bool], Tuple[int, bool]]:
        """
        Finds the two inputs for a certain component.
        (helper function for making fault assumptions)

        :param comp_out: the component to find the inputs for
        :return: Index of component a and b,
                 bool if it can be found in observations or behaviour in file
        """
        match_a = re.search(rf"(IN1\({comp_out}\))=(0|1|OUT\((.+)\))\n", self.document)
        match_b = re.search(rf"(IN2\({comp_out}\))=(0|1|OUT\((.+)\))\n", self.document)

        if not match_a or not match_b:
            raise Exception("No two inputs found for this component.")
        else:
            in_variables_strings = [str(variable) for variable in self.in_variables]
            comp_out_strings = [str(comp) for comp in self.all_gates]

            # if input_a is from an observation:
            if match_a.group(2) == "0" or match_a.group(2) == "1":
                in_a = in_variables_strings.index(match_a.group(1))
                obs_a = True
            # if input_a arises from circuit behaviour:
            else:
                in_a = comp_out_strings.index(match_a.group(3))
                obs_a = False
            # if input_b is from an observation:
            if match_b.group(2) == "0" or match_b.group(2) == "1":
                in_b = in_variables_strings.index(match_b.group(1))
                obs_b = True
            # if input_b arises from circuit behaviour:
            else:
                in_b = comp_out_strings.index(match_b.group(3))
                obs_b = False

            return (in_a, obs_a), (in_b, obs_b)
        

    def make_fault_assumptions(self) -> List[z3.z3.BoolRef]:
        """
        Function that makes a list of fault assumptions.

        :return: list of fault assumptions
        """
        fault_assumptions = []

        # a fault assumption is needed for every single gate:
        for comp, comp_out in zip(self.comps, self.all_gates):
            gate = self.find_corresponding_gate_type(comp_out)
            (input_a, obs_a), (input_b, obs_b) = self.find_inputs(comp_out)

            # handling which in-variables or components to take for each gate:
            if obs_a:
                input_a = self.in_variables[input_a]
            else:
                input_a = self.comps[input_a]
            if obs_b:
                input_b = self.in_variables[input_b]
            else:
                input_b = self.comps[input_b]

            # fault assumption consists of:
            # - component
            # - gate type
            # - input a & b
            # - component output
            fault_assumptions.append(self.faulted(comp, gate(input_a, input_b), comp_out))

        return fault_assumptions


    def extract_observations(self) -> List[z3.z3.BoolRef]:
        """
        Extracts both the in- and out-observations, with their truth value.

        :return: list of observation with truth values.
        """
        match = re.search(r"\s*OBSERVATIONS:(.*?)ENDOBSERVATIONS", self.document, re.DOTALL)
        all_observations = []
        if not match:
            raise Exception("No observations found.")
        else:
            observations_string = match.group(1)
            observations = observations_string.split("\n")
            observations = list(filter(len, observations))  # remove empty strings

            for i, observation in enumerate(observations):
                value = bool(int(observation.split("=")[1]))
                all_observations.append(self.in_variables[i] == value)

        match = re.search(r"\s*OUTOBSERVATIONS:(.*?)ENDOUTOBSERVATIONS", self.document, re.DOTALL)
        if not match:
            raise Exception("No out observations found.")
        else:
            observations_string = match.group(1)
            observations = observations_string.split("\n")
            observations = list(filter(len, observations))  # remove empty strings

            for observation in observations:
                for j, comp in enumerate(self.all_gates):
                    component_match = re.search(r"OUT\((.+)\)", observation)
                    if not component_match:
                        raise Exception("No component found")
                    if component_match.group(1) == str(comp):
                        value = bool(int(observation.split("=")[1]))
                        all_observations.append(self.comps[j] == value)
        return all_observations


    @staticmethod
    def powerset(s) -> chain[Tuple[z3.z3.BoolRef]]:
        """
        Generate a powerset of s, excluding the empty set.
        Used for brute-forcing checking all combinations of gates.

        :param s: iterable of which to generate powerset
        :return: powerset of s
        """
        return chain.from_iterable(combinations(s, r) for r in range(1, len(s) + 1))


    def retrieve_conflict_sets(self) -> List[List[str]]:
        """
        Handles all z3 logic to retrieve the conflict sets.

        :return: list of list of z3 variables, which are the conflict sets.
        """
        conflict_sets = []
        s = Solver()

        for healthy_combo in self.powerset(self.all_gates):
            s.add(*self.fault_assumptions, *self.observations)

            # Assume gates in the combination are healthy (fault_flag == False)
            for g in self.all_gates:
                if g in healthy_combo:
                    s.add(g == False)  # Healthy
                else:
                    s.add(g == True)  # Faulty

            # If assuming all of these are healthy leads to contradiction -> it is a conflict set
            if s.check() == unsat:
                conflict_sets.append(set(healthy_combo))

            s.reset()

        minimal_conflicts = []
        for cs in conflict_sets:
            if all(not cs > other for other in conflict_sets):
                minimal_conflicts.append(list(cs))

        return [[str(item) for item in sublist] for sublist in minimal_conflicts]
