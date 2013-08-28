package net.trevize.labelme;


import java.io.Serializable;
import java.util.HashMap;

import org.apache.commons.lang.mutable.MutableInt;

/**
 * This class represents a SingleAnnotation, i.e. encapsulating a String
 * which is an annotation (clearly containing a sense unit), and 
 * the usage (or multiplicity) of this annotation in the LabelMe dataset.
 * 
 * @author Nicolas James <nicolas.james@gmail.com> [[http://njames.trevize.net]]
 *
 */

public class SingleAnnotation implements Serializable {
	public String filepath = null;

	public String annotation = null;

	public MutableInt multiplicity = null;

	public HashMap<String, MutableInt> cooccurrences = null;

	public SingleAnnotation(String filepath, String annotation,
			MutableInt multiplicity) {
		this.filepath = filepath;
		this.annotation = annotation;
		this.multiplicity = multiplicity;
		cooccurrences = new HashMap<String, MutableInt>();
	}
}