package uk.co.saiman.chemistry;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.TreeSet;

import uk.co.saiman.chemistry.isotope.Isotope;

public class ChemicalComposition {
	// elements of unspecified isotope in this molecule
	private final TreeMap<Element, Integer> elements;
	// elements of specified isotope in this molecule
	private final TreeMap<Isotope, Integer> isotopes;

	private final int charge;

	// create an empty molecule
	public ChemicalComposition() {
		this(1);
	}

	// create an empty molecule
	public ChemicalComposition(int charge) {
		elements = new TreeMap<Element, Integer>();
		isotopes = new TreeMap<Isotope, Integer>();
		this.charge = charge;
	}

	private ChemicalComposition(ChemicalComposition molecule) {
		elements = new TreeMap<>(molecule.elements);
		isotopes = new TreeMap<>(molecule.isotopes);
		charge = molecule.charge;
	}

	private ChemicalComposition copy() {
		return new ChemicalComposition(this);
	}

	// add a single element to the molecule
	public ChemicalComposition withElement(Element element) {
		ChemicalComposition merged = copy();

		if (merged.elements.containsKey(element)) {
			merged.elements.put(element, merged.elements.get(element) + 1);
		} else {
			merged.elements.put(element, 1);
		}

		return merged;
	}

	// add a single isotope to the molecule
	public ChemicalComposition withIsotope(Isotope isotope) {
		ChemicalComposition merged = copy();

		if (merged.isotopes.containsKey(isotope)) {
			merged.isotopes.put(isotope, merged.isotopes.get(isotope) + 1);
		} else {
			merged.isotopes.put(isotope, 1);
		}

		return merged;
	}

	// add a certain amount of a single element to the molecule
	public ChemicalComposition withElement(Element element, int count) {
		ChemicalComposition merged = copy();

		if (merged.elements.containsKey(element)) {
			merged.elements.put(element, merged.elements.get(element) + count);
		} else {
			merged.elements.put(element, count);
		}

		return merged;
	}

	// add a certain amount of a single isotope to the molecule
	public ChemicalComposition withIsotope(Isotope isotope, int count) {
		ChemicalComposition merged = copy();

		if (merged.isotopes.containsKey(isotope)) {
			merged.isotopes.put(isotope, merged.isotopes.get(isotope) + count);
		} else {
			merged.isotopes.put(isotope, count);
		}

		return merged;
	}

	// add one of each of a set of elements to the molecule
	public ChemicalComposition withElements(List<Element> elements) {
		ChemicalComposition merged = copy();

		Iterator<Element> elementIterator = elements.iterator();
		Element element;
		while (elementIterator.hasNext()) {
			element = elementIterator.next();
			if (merged.elements.containsKey(element)) {
				merged.elements.put(element, merged.elements.get(element) + 1);
			} else {
				merged.elements.put(element, 1);
			}
		}

		return merged;
	}

	// add one of each of a set of elements to the molecule
	public ChemicalComposition withIsotopes(List<Isotope> isotopes) {
		ChemicalComposition merged = copy();

		Iterator<Isotope> isotopeIterator = isotopes.iterator();
		Isotope isotope;
		while (isotopeIterator.hasNext()) {
			isotope = isotopeIterator.next();
			if (merged.isotopes.containsKey(isotope)) {
				merged.isotopes.put(isotope, merged.isotopes.get(isotope) + 1);
			} else {
				merged.isotopes.put(isotope, 1);
			}
		}

		return merged;
	}

	// add a certain amount of each of a set of elements to the molecule
	public ChemicalComposition withElements(List<Element> elements, int count) {
		ChemicalComposition merged = copy();

		Iterator<Element> elementIterator = elements.iterator();
		Element element;
		while (elementIterator.hasNext()) {
			element = elementIterator.next();
			if (merged.elements.containsKey(element)) {
				merged.elements.put(element, merged.elements.get(element) + count);
			} else {
				merged.elements.put(element, count);
			}
		}

		return merged;
	}

	// add a certain amount of each of a set of elements to the molecule
	public ChemicalComposition withIsotopes(List<Isotope> isotopes, int count) {
		ChemicalComposition merged = copy();

		Iterator<Isotope> isotopeIterator = isotopes.iterator();
		Isotope isotope;
		while (isotopeIterator.hasNext()) {
			isotope = isotopeIterator.next();
			if (merged.isotopes.containsKey(isotope)) {
				merged.isotopes.put(isotope, merged.isotopes.get(isotope) + count);
			} else {
				merged.isotopes.put(isotope, count);
			}
		}
		return this;
	}

	// add different amounts of each of a set of elements to the molecule
	public ChemicalComposition withElements(Map<Element, Integer> elements) {
		ChemicalComposition merged = copy();

		Iterator<Element> elementIterator = elements.keySet().iterator();
		Element element;
		int count;
		while (elementIterator.hasNext()) {
			element = elementIterator.next();
			count = elements.get(element);
			if (merged.elements.containsKey(element)) {
				merged.elements.put(element, merged.elements.get(element) + count);
			} else {
				merged.elements.put(element, count);
			}
		}

		return merged;
	}

	// add different amounts of each of a set of elements to the molecule
	public ChemicalComposition withIsotopes(Map<Isotope, Integer> isotopes) {
		ChemicalComposition merged = copy();

		Iterator<Isotope> isotopeIterator = isotopes.keySet().iterator();
		Isotope isotope;
		int count;
		while (isotopeIterator.hasNext()) {
			isotope = isotopeIterator.next();
			count = isotopes.get(isotope);
			if (merged.isotopes.containsKey(isotope)) {
				merged.isotopes.put(isotope, merged.isotopes.get(isotope) + count);
			} else {
				merged.isotopes.put(isotope, count);
			}
		}

		return merged;
	}

	// merge with an existing molecule
	public ChemicalComposition withMolecule(ChemicalComposition molecule) {
		ChemicalComposition merged = this;

		merged.withElements(molecule.getElementCounts());
		merged.withIsotopes(molecule.getIsotopeCounts());

		return merged;
	}

	public ChemicalComposition withCharge(int charge) {
		return new ChemicalComposition(charge).withMolecule(this);
	}

	public int getCharge() {
		return charge;
	}

	public void clear() {
		elements.clear();
	}

	public ChemicalComposition withMultipliedCounts(int value) {
		ChemicalComposition molecule = copy();

		if (value < 0)
			throw new IllegalArgumentException();

		if (value == 0) {
			molecule.elements.clear();
			molecule.isotopes.clear();
		} else {
			Iterator<Element> elementIterator = molecule.elements.keySet().iterator();
			Element element;
			while (elementIterator.hasNext()) {
				element = elementIterator.next();
				molecule.elements.put(element, molecule.elements.get(element) * value);
			}
			Iterator<Isotope> isotopeIterator = molecule.isotopes.keySet().iterator();
			Isotope isotope;
			while (isotopeIterator.hasNext()) {
				isotope = isotopeIterator.next();
				molecule.isotopes.put(isotope, molecule.isotopes.get(isotope) * value);
			}
		}

		return molecule;
	}

	public SortedMap<Element, Integer> getElementCounts() {
		SortedMap<Element, Integer> elements = new TreeMap<Element, Integer>();
		elements.putAll(this.elements);
		return elements;
	}

	public SortedMap<Isotope, Integer> getIsotopeCounts() {
		SortedMap<Isotope, Integer> isotopes = new TreeMap<Isotope, Integer>();
		isotopes.putAll(this.isotopes);
		return isotopes;
	}

	public TreeSet<Element> getElements() {
		TreeSet<Element> elements = new TreeSet<Element>();
		elements.addAll(this.elements.keySet());
		return elements;
	}

	public TreeSet<Isotope> getIsotopes() {
		TreeSet<Isotope> isotopes = new TreeSet<Isotope>();
		isotopes.addAll(this.isotopes.keySet());
		return isotopes;
	}

	public double getAverageMass() {
		return getAverageMassEffectiveAtCharge(1);
	}

	public double getAverageMassEffectiveAtCharge(int charge) {
		double mass = 0;

		Iterator<Element> elementIterator = elements.keySet().iterator();
		Element element;
		while (elementIterator.hasNext()) {
			element = elementIterator.next();

			mass += element.getAverageMass() * elements.get(element);
		}

		Iterator<Isotope> isotopeIterator = isotopes.keySet().iterator();
		Isotope isotope;
		while (isotopeIterator.hasNext()) {
			isotope = isotopeIterator.next();

			mass += isotope.getMass() * isotopes.get(isotope);
		}

		if (charge != 0) {
			mass /= Math.abs(charge);
		}

		return mass;
	}

	public double getMonoisotopicMass() {
		return getMonoisotopicMassEffectiveAtCharge(1);
	}

	public double getMonoisotopicMassEffectiveAtCharge(int charge) {
		double mass = 0;

		Iterator<Element> elementIterator = elements.keySet().iterator();
		Element element;
		while (elementIterator.hasNext()) {
			element = elementIterator.next();

			mass += element.getMonoisotopicMass() * elements.get(element);
		}

		Iterator<Isotope> isotopeIterator = isotopes.keySet().iterator();
		Isotope isotope;
		while (isotopeIterator.hasNext()) {
			isotope = isotopeIterator.next();

			mass += isotope.getMass() * isotopes.get(isotope);
		}

		if (charge != 0) {
			mass /= Math.abs(charge);
		}

		return mass;
	}

	public boolean containsElementOrIsotopeOf(Element element) {
		Iterator<Isotope> isotopeIterator = isotopes.keySet().iterator();
		while (isotopeIterator.hasNext()) {
			if (isotopeIterator.next().getElement() == element) {
				return true;
			}
		}
		return elements.containsKey(element);
	}

	public boolean containsElement(Element element) {
		return elements.containsKey(element);
	}

	public boolean containsIsotopeOf(Element element) {
		Iterator<Isotope> isotopeIterator = isotopes.keySet().iterator();
		while (isotopeIterator.hasNext()) {
			if (isotopeIterator.next().getElement() == element) {
				return true;
			}
		}
		return false;
	}

	public boolean containsIsotope(Isotope isotope) {
		return isotopes.containsKey(isotope);
	}

	public int elementCount(Element element) {
		if (elements.containsKey(element)) {
			return elements.get(element);
		}
		return 0;
	}

	public int atomCount() {
		int count = 0;
		Iterator<Integer> elementIterator;
		elementIterator = elements.values().iterator();
		while (elementIterator.hasNext()) {
			count += elementIterator.next().intValue();
		}
		elementIterator = isotopes.values().iterator();
		while (elementIterator.hasNext()) {
			count += elementIterator.next();
		}
		return count;
	}

	public int elementOrIsotopeOfCount(Element element) {
		int count = 0;
		Iterator<Isotope> isotopeIterator = isotopes.keySet().iterator();
		Isotope isotope;
		while (isotopeIterator.hasNext()) {
			isotope = isotopeIterator.next();
			if (isotope.getElement() == element) {
				count += isotopes.get(isotope);
			}
		}
		if (elements.containsKey(element)) {
			count += elements.get(element);
		}
		return count;
	}

	public int isotopeCount(Isotope isotope) {
		if (isotopes.containsKey(isotope)) {
			return isotopes.get(isotope);
		}
		return 0;
	}

	public int isotopeOfCount(Element element) {
		int count = 0;
		Iterator<Isotope> isotopeIterator = isotopes.keySet().iterator();
		Isotope isotope;
		while (isotopeIterator.hasNext()) {
			isotope = isotopeIterator.next();
			if (isotope.getElement() == element) {
				count += isotopes.get(isotope);
			}
		}
		return count;
	}

	@Override
	public String toString() {
		String name = "";
		int count;

		Iterator<Element> elementIterator = elements.keySet().iterator();
		while (elementIterator.hasNext()) {
			Element element = elementIterator.next();
			count = elements.get(element);
			name += element.getSymbol();
			if (count > 1) {
				name += count;
			}
		}

		Iterator<Isotope> isotopeIterator = isotopes.keySet().iterator();
		while (isotopeIterator.hasNext()) {
			Isotope isotope = isotopeIterator.next();
			count = isotopes.get(isotope);
			name += "^" + isotope.getMassNumber() + isotope.getElement().getSymbol();
			if (count > 1) {
				name += count;
			}
		}

		return name;
	}

	public String toHTMLString() {
		String name = "";
		int count;

		Iterator<Element> elementIterator = elements.keySet().iterator();
		while (elementIterator.hasNext()) {
			Element element = elementIterator.next();
			count = elements.get(element);
			name += element.getSymbol();
			if (count > 1) {
				name += "<sub>" + count + "</sub>";
			}
		}

		Iterator<Isotope> isotopeIterator = isotopes.keySet().iterator();
		while (isotopeIterator.hasNext()) {
			Isotope isotope = isotopeIterator.next();
			count = isotopes.get(isotope);
			name += "<sup>" + isotope.getMassNumber() + "</sup>" + isotope.getElement().getSymbol();
			if (count > 1) {
				name += "<sub>" + count + "</sub>";
			}
		}

		return name;
	}

	@Override
	public boolean equals(Object that) {
		if (this == that)
			return true;

		if (!(that instanceof ChemicalComposition))
			return false;

		ChemicalComposition thatMolecule = (ChemicalComposition) that;

		if (!this.elements.equals(thatMolecule.elements) || !this.isotopes.equals(thatMolecule.isotopes))
			return false;

		return true;
	}

	@Override
	public int hashCode() {
		return elements.hashCode() ^ isotopes.hashCode();
	}
}
