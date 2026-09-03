import schemdraw
import schemdraw.logic as logic
import schemdraw.elements as elm
import webbrowser


def plot_circuit(document):
    if document == 'circuit1.txt':
        plot_circuit_1()
    elif document == 'circuit2.txt':
        plot_circuit_2()
    elif document == 'circuit3.txt':
        plot_circuit_3()
    elif document == 'circuit4.txt':
        plot_circuit_4()
    elif document == 'circuit5.txt':
        plot_circuit_5()
    elif document == 'circuit6.txt':
        plot_circuit_6()
    elif document == 'circuit7.txt':
        plot_circuit_7()
    else:
        print("Sorry, I do not have instructions on how to plot this circuit.")


def plot_circuit_1():
    with schemdraw.Drawing(file='.circuit.svg', show=False) as d:
        # Components
        x1 = d.add(logic.Xor(inputs=2).label('X1').at((0,0.25)))
        a1 = d.add(logic.And(inputs=2).label('A1').at((4,-2)))
        a2 = d.add(logic.And(inputs=2).label('A2').at((4,2)))
        x2 = d.add(logic.Xor(inputs=2).label('X2').at((4,0)))
        o1 = d.add(logic.Or(inputs=2).label('O1').at((8,0)))

        # Behaviour connections (manual wiring)
        d.add(elm.Line().at(x1.out).to(x2.in1))
        d.add(elm.Line().at(x1.out).to(a2.in2))
        d.add(elm.Line().at(a2.out).to(o1.in1))
        d.add(elm.Line().at(a1.out).to(o1.in2))

        # Inputs annotations (labels near inputs)
        d.add(elm.Dot().at(x1.in1).label('1', loc='left'))
        d.add(elm.Dot().at(x1.in2).label('0', loc='left'))
        d.add(elm.Dot().at(x2.in2).label('1', loc='left'))
        d.add(elm.Dot().at(a1.in1).label('1', loc='left'))
        d.add(elm.Dot().at(a1.in2).label('0', loc='left'))
        d.add(elm.Dot().at(a2.in1).label('1', loc='left'))

        # Output observations
        d.add(elm.Dot().at(o1.out).label('0', loc='right'))
        d.add(elm.Dot().at(x2.out).label('1', loc='right'))

    webbrowser.open('.circuit.svg')


def plot_circuit_2():
    with schemdraw.Drawing(file='.circuit.svg', show=False) as d:
        # Components (4 XORs)
        x1 = d.add(logic.Xor(inputs=2).label('X1').at((-1, 2)))
        x2 = d.add(logic.Xor(inputs=2).label('X2').at((-1, 0)))
        x3 = d.add(logic.Xor(inputs=2).label('X3').at((2,  1)))
        x4 = d.add(logic.Xor(inputs=2).label('X4').at((2, -1)))

        # Behaviour connections
        d.add(elm.Line().at(x1.out).to(x3.in1))
        d.add(elm.Line().at(x2.out).to(x3.in2))
        d.add(elm.Line().at(x2.out).to(x4.in1))

        # Input annotations
        d.add(elm.Dot().at(x1.in1).label('0', loc='left'))
        d.add(elm.Dot().at(x1.in2).label('1', loc='left'))
        d.add(elm.Dot().at(x2.in1).label('0', loc='left'))
        d.add(elm.Dot().at(x2.in2).label('1', loc='left'))
        d.add(elm.Dot().at(x4.in2).label('0', loc='left'))

        # Output observations
        d.add(elm.Dot().at(x3.out).label('1', loc='right'))
        d.add(elm.Dot().at(x4.out).label('0', loc='right'))

    webbrowser.open('.circuit.svg')


def plot_circuit_3():
    with schemdraw.Drawing(file='.circuit.svg', show=False) as d:
        # XOR gate
        x = d.add(logic.Xor(inputs=2).label('X'))
        # Place AND gate below
        a = d.add(logic.And(inputs=2).label('A').at((0, -3)))

        # Input annotations
        d.add(elm.Dot().at(x.in1).label('0', loc='left'))
        d.add(elm.Dot().at(x.in2).label('1', loc='left'))
        d.add(elm.Dot().at(a.in1).label('0', loc='left'))
        d.add(elm.Dot().at(a.in2).label('1', loc='left'))

        # Output observations
        d.add(elm.Dot().at(x.out).label('0', loc='right'))
        d.add(elm.Dot().at(a.out).label('1', loc='right'))

    webbrowser.open('circuitssvg/circuit3.svg')


def plot_circuit_4():
    with schemdraw.Drawing(file='.circuit.svg', show=False) as d:
        # Gates
        a1 = d.add(logic.And(inputs=2).label('A1').at((3, 4)))
        o1 = d.add(logic.Or(inputs=2).label('O1').at((3, 2)))
        a2 = d.add(logic.And(inputs=2).label('A2').at((3, 0)))
        a3 = d.add(logic.And(inputs=2).label('A3').at((6, 1)))
        o2 = d.add(logic.Or(inputs=2).label('O2').at((9, 1)))

        # Connections
        d.add(elm.Line().at(o1.out).to(a3.in1))
        d.add(elm.Line().at(a2.out).to(a3.in2))
        d.add(elm.Line().at(a1.out).to(o2.in1))
        d.add(elm.Line().at(a3.out).to(o2.in2))

        # Input labels
        d.add(elm.Dot().at(a1.in1).label('0', loc='left'))
        d.add(elm.Dot().at(a1.in2).label('1', loc='left'))
        d.add(elm.Dot().at(o1.in1).label('0', loc='left'))
        d.add(elm.Dot().at(o1.in2).label('0', loc='left'))
        d.add(elm.Dot().at(a2.in1).label('0', loc='left'))
        d.add(elm.Dot().at(a2.in2).label('1', loc='left'))

        # Output label
        d.add(elm.Dot().at(o2.out).label('1', loc='right'))

    webbrowser.open('.circuit.svg')

def plot_circuit_5():
    with schemdraw.Drawing(file='.circuit.svg', show=False) as d:
        # Gates in a row
        a1 = d.add(logic.And(inputs=2).label('A1').at((0, 2)))
        a2 = d.add(logic.And(inputs=2).label('A2').at((2, 1.5)))
        a3 = d.add(logic.And(inputs=2).label('A3').at((4, 1)))
        a4 = d.add(logic.And(inputs=2).label('A4').at((6, 0.5)))
        a5 = d.add(logic.And(inputs=2).label('A5').at((8, 0)))

        # Connections
        d.add(elm.Line().at(a1.out).to(a2.in1))
        d.add(elm.Line().at(a2.out).to(a3.in1))
        d.add(elm.Line().at(a3.out).to(a4.in1))
        d.add(elm.Line().at(a4.out).to(a5.in1))

        # Input labels
        d.add(elm.Dot().at(a1.in1).label('1', loc='left'))
        d.add(elm.Dot().at(a1.in2).label('1', loc='left'))
        d.add(elm.Dot().at(a2.in2).label('1', loc='left'))
        d.add(elm.Dot().at(a3.in2).label('1', loc='left'))
        d.add(elm.Dot().at(a4.in2).label('1', loc='left'))
        d.add(elm.Dot().at(a5.in2).label('1', loc='left'))

        # Output label
        d.add(elm.Dot().at(a5.out).label('0', loc='right'))

    webbrowser.open('.circuit.svg')


def plot_circuit_6():
    with schemdraw.Drawing(file='.circuit.svg', show=False) as d:
        a1 = d.add(logic.And(inputs=2).label('A1'))
        a2 = d.add(logic.And(inputs=2).label('A2').at((0, -2)))
        a3 = d.add(logic.And(inputs=2).label('A3').at((0, -4)))
        a4 = d.add(logic.And(inputs=2).label('A4').at((0, -6)))

        # OR gates
        o1 = d.add(logic.Or(inputs=2).label('O1').at((3, -1)))
        o2 = d.add(logic.Or(inputs=2).label('O2').at((3, -5)))

        # XOR gate
        x1 = d.add(logic.Xor(inputs=2).label('X1').at((6, -3)))

        # Connections
        d.add(elm.Line().at(a1.out).to(o1.in1))
        d.add(elm.Line().at(a2.out).to(o1.in2))
        d.add(elm.Line().at(a3.out).to(o2.in1))
        d.add(elm.Line().at(a4.out).to(o2.in2))
        d.add(elm.Line().at(o1.out).to(x1.in1))
        d.add(elm.Line().at(o2.out).to(x1.in2))

        # Input labels
        d.add(elm.Dot().at(a1.in1).label('1', loc='left'))
        d.add(elm.Dot().at(a1.in2).label('1', loc='left'))
        d.add(elm.Dot().at(a2.in1).label('1', loc='left'))
        d.add(elm.Dot().at(a2.in2).label('1', loc='left'))
        d.add(elm.Dot().at(a3.in1).label('0', loc='left'))
        d.add(elm.Dot().at(a3.in2).label('0', loc='left'))
        d.add(elm.Dot().at(a4.in1).label('0', loc='left'))
        d.add(elm.Dot().at(a4.in2).label('0', loc='left'))

        # Output observation
        d.add(elm.Dot().at(x1.out).label('0', loc='right'))

    webbrowser.open('.circuit.svg')


def plot_circuit_7():
    with schemdraw.Drawing(file='.circuit.svg', show=False) as d:
        a1 = d.add(logic.And(inputs=2).label('A1'))
        a2 = d.add(logic.And(inputs=2).label('A2').at((0, -3)))
        a3 = d.add(logic.And(inputs=2).label('A3').at((0, -6)))
        a4 = d.add(logic.And(inputs=2).label('A4').at((9, -1.5)))
        a5 = d.add(logic.And(inputs=2).label('A5').at((9, -4.5)))

        # OR gates
        o1 = d.add(logic.Or(inputs=2).label('O1').at((3, -1.5)))
        o2 = d.add(logic.Or(inputs=2).label('O2').at((3, -4.5)))
        o3 = d.add(logic.Or(inputs=2).label('O3').at((12, 0)))
        o4 = d.add(logic.Or(inputs=2).label('O4').at((12, -3)))
        o5 = d.add(logic.Or(inputs=2).label('O5').at((12, -6)))

        # XOR gate
        x1 = d.add(logic.Xor(inputs=2).label('X1').at((6, -3)))

        # Connections
        d.add(elm.Line().at(a1.out).to(o1.in1))
        d.add(elm.Line().at(a2.out).to(o1.in2))
        d.add(elm.Line().at(a2.out).to(o2.in1))
        d.add(elm.Line().at(a3.out).to(o2.in2))
        d.add(elm.Line().at(o1.out).to(x1.in1))
        d.add(elm.Line().at(o2.out).to(x1.in2))
        d.add(elm.Line().at(x1.out).to(a4.in2))
        d.add(elm.Line().at(x1.out).to(a5.in1))
        d.add(elm.Line().at(a4.out).to(o3.in2))
        d.add(elm.Line().at(a4.out).to(o4.in1))
        d.add(elm.Line().at(a5.out).to(o4.in2))
        d.add(elm.Line().at(a5.out).to(o5.in1))

        # Input labels
        d.add(elm.Dot().at(a1.in1).label('1', loc='left'))
        d.add(elm.Dot().at(a1.in2).label('1', loc='left'))
        d.add(elm.Dot().at(a2.in1).label('0', loc='left'))
        d.add(elm.Dot().at(a2.in2).label('1', loc='left'))
        d.add(elm.Dot().at(a3.in1).label('0', loc='left'))
        d.add(elm.Dot().at(a3.in2).label('1', loc='left'))
        d.add(elm.Dot().at(a4.in1).label('1', loc='left'))
        d.add(elm.Dot().at(a5.in2).label('0', loc='left'))
        d.add(elm.Dot().at(o3.in1).label('0', loc='left'))
        d.add(elm.Dot().at(o5.in2).label('1', loc='left'))

        # Output observations
        d.add(elm.Dot().at(o3.out).label('0', loc='right'))
        d.add(elm.Dot().at(o4.out).label('1', loc='right'))
        d.add(elm.Dot().at(o5.out).label('0', loc='right'))

    webbrowser.open('.circuit.svg')