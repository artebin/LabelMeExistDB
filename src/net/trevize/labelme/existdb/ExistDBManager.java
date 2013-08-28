package net.trevize.labelme.existdb;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import net.trevize.labelme.SingleAnnotation;

import org.apache.commons.lang.mutable.MutableInt;
import org.xmldb.api.DatabaseManager;
import org.xmldb.api.base.Collection;
import org.xmldb.api.base.Database;
import org.xmldb.api.base.Resource;
import org.xmldb.api.base.ResourceIterator;
import org.xmldb.api.base.ResourceSet;
import org.xmldb.api.base.XMLDBException;
import org.xmldb.api.modules.CollectionManagementService;
import org.xmldb.api.modules.XMLResource;
import org.xmldb.api.modules.XPathQueryService;

/**
 * This class is a database manager for the eXistDB for mining the LabelMe
 * annotations dataset.
 * See [[http://njames.trevize.net/wiki/doku.php/exist_db]].
 * 
 * @author Nicolas James <nicolas.james@gmail.com> [[http://njames.trevize.net]]
 * ExistDBManager.java - Jan 16, 2009
 */

public class ExistDBManager {
	private static final String EXIST_URI = "xmldb:exist://localhost:8080/exist/xmlrpc";

	private Database db;

	private DatabaseManager dbm;

	private String collectionName = "/db/LabelMe";

	public ExistDBManager() {
		String driver = "org.exist.xmldb.DatabaseImpl";

		// initialize database driver  
		try {
			Class cl = Class.forName(driver);
			Database database = (Database) cl.newInstance();
			DatabaseManager.registerDatabase(database);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (XMLDBException e) {
			e.printStackTrace();
		}
	}

	public void indexDataset(String dirpath) {
		// try to get collection.
		Collection col = null;
		try {
			col = DatabaseManager.getCollection(EXIST_URI);
		} catch (XMLDBException e) {
			e.printStackTrace();
		}

		//if collection ever exists, delete it.
		if (col != null) {
			CollectionManagementService svc = null;
			try {
				svc = (CollectionManagementService) col.getService(
						"CollectionManager", "1.0");
				svc.removeCollection(collectionName);
			} catch (XMLDBException e) {
				e.printStackTrace();
			}
			col = null;
		}

		if (col == null) {
			// collection does not exist: get root collection and create  
			// for simplicity, we assume that the new collection is a  
			// direct child of the root collection, e.g. /db/test.  
			// the example will fail otherwise.

			try {
				Collection root = DatabaseManager.getCollection(EXIST_URI
						+ "/db");
				CollectionManagementService mgtService = (CollectionManagementService) root
						.getService("CollectionManagementService", "1.0");
				col = mgtService.createCollection(collectionName);
			} catch (XMLDBException e) {
				e.printStackTrace();
			}
		}

		indexDir(dirpath, col);
		System.out.println("End indexDataset.");
	}

	private void indexDir(String dirpath, Collection col) {
		System.out.println("Storing in eXist for directory: " + dirpath);

		File[] lf = new File(dirpath).listFiles();

		for (File f : lf) {
			if (f.isDirectory()) {
				try {
					//create a new collection for this directory.
					Collection subcol = null;
					try {
						CollectionManagementService mgtService = (CollectionManagementService) col
								.getService("CollectionManagementService",
										"1.0");
						subcol = mgtService.createCollection(f.getName());
					} catch (XMLDBException e) {
						e.printStackTrace();
					}

					indexDir(f.getCanonicalPath(), subcol);
				} catch (IOException e) {
					e.printStackTrace();
				}
			} else {
				if (f.getName().endsWith(".xml")) {
					try {
						indexFile(f.getCanonicalPath(), col);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}

	private void indexFile(String filepath, Collection col) {
		// create new XMLResource; an id will be assigned to the new resource  
		try {
			XMLResource document = (XMLResource) col.createResource(null,
					"XMLResource");
			File f = new File(filepath);
			if (!f.canRead()) {
				System.out.println("cannot read file " + filepath);
				return;
			}
			document.setContent(f);
			col.storeResource(document);
		} catch (XMLDBException e) {
			e.printStackTrace();
		}
	}

	public void extractRawVocabulary() {
		try {
			Collection col = DatabaseManager.getCollection(EXIST_URI
					+ collectionName);
			XPathQueryService svc = (XPathQueryService) col.getService(
					"XPathQueryService", "1.0");
			svc.setProperty("indent", "no");
			ResourceSet result = svc.query("/annotation/object/name/text()");

			ResourceIterator i = result.getIterator();

			HashMap<String, SingleAnnotation> vocabulary = new HashMap<String, SingleAnnotation>();

			while (i.hasMoreResources()) {
				Resource r = i.nextResource();

				String annot = r.getContent().toString();

				annot = trimpp(annot);

				if (vocabulary.keySet().contains(annot)) {
					vocabulary.get(annot).multiplicity.increment();
				} else {
					vocabulary.put(annot, new SingleAnnotation("", annot,
							new MutableInt(1)));
				}
			}

			System.out.println(vocabulary.size());

		} catch (XMLDBException e) {
			e.printStackTrace();
		}

		System.out.println("End extractRawVocabulary.");
	}

	/**
	 * An augmented version of the String.trim() method which remove '\n', '\r'
	 * and '\t'.
	 * @param s
	 * @return
	 */
	private String trimpp(String s) {
		s = s.trim();
		s = s.replaceAll("\r", " ");
		s = s.replaceAll("\n", " ");
		s = s.replaceAll("\t", " ");

		/*
		 * It could be a problem to transform '\t' by '', it could be more
		 * interesting to tranform '\t' by ' ' and to transform '\s+' by 
		 * the String ' '. 
		 */
		s = s.replaceAll("\\s+", " ");

		return s;
	}

	public static void main(String args[]) {
		String dirpath = "/home/nicolas/dataset/LabelMe/database/annotations";

		ExistDBManager edbm = new ExistDBManager();

		edbm.indexDataset(dirpath);

		//		edbm.extractRawVocabulary();
	}
}
