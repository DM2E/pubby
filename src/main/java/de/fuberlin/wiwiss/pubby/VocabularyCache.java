package de.fuberlin.wiwiss.pubby;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.RDFS;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Logger;

/**
 * The vocabulary cache is used to store labels and descriptions of classes and properties.
 *
 * @author Kai Eckert (kai@informatik.uni-mannheim.de)
 * @version $Id$
 */
public class VocabularyCache {

    private Configuration configuration;
    private Logger log = Logger.getLogger(getClass().getName());
    private Model cache = ModelFactory.createDefaultModel();
    private Map labelCache = new HashMap();
    private Map descriptionCache = new HashMap();
    private boolean deactivated = false;

    public VocabularyCache(Configuration configuration) {
        this.configuration = configuration;
        if (!configuration.showLabels()) {
            deactivated = true;
            return;
        }
        Iterator it = configuration.getExternalVocabularyURLs().iterator();
        while (it.hasNext()) {
            String next = (String) it.next();
            try {
            } catch (Throwable t) {
                log.warning("Could not read: " + next);
            }
        }
    }

    public String getLabel(String uri) {
        if (deactivated) return uri;
        return getLabel(uri, configuration.getDefaultLanguage());
    }

    public String getLabel(String uri, String language) {
        if (deactivated) return uri;
        if (labelCache.containsKey(uri)) return (String) labelCache.get(uri);
        String label = getProperty(uri, RDFS.label, uri, true);
        labelCache.put(uri,label);
        return label;

    }

    public String getDescription(String uri) {
        if (deactivated) return "";
        return getDescription(uri,configuration.getDefaultLanguage());
    }

    public String getDescription(String uri, String language) {
        if (deactivated) return "";
        if (descriptionCache.containsKey(uri)) return (String) descriptionCache.get(uri);
        String desc = getProperty(uri, RDFS.comment, "", true);
        descriptionCache.put(uri,desc);
        return desc;

    }

    protected String getProperty(String uri, Property prop, String defaultValue, boolean fetch) {
        log.fine("Fetching property labels");
        String result = defaultValue;
        try {
            if (fetch && !cache.contains(cache.getResource(uri), prop)) {
                cache.read(uri);
            }

            Resource res = cache.getResource(uri);
            result = cache.getResource(uri).getProperty(prop).getString();
            result = beautify(result);
        } catch (Throwable t) {
            log.fine("Exception while fetching " + prop.getURI() + " for " + uri + ": " + t);
        }
        return result;
    }

    protected String beautify(String input) {
        input = input.substring(0,1).toUpperCase() + input.substring(1);
        input = input.replaceAll("([a-z]{1})([A-Z]{1})","$1 $2");
        return input;
    }
}
