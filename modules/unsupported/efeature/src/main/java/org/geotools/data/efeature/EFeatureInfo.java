package org.geotools.data.efeature;

import static org.geotools.data.efeature.EFeatureConstants.DEFAULT_SRID;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;
import org.geotools.feature.NameImpl;
import org.geotools.referencing.CRS;
import org.opengis.feature.Feature;
import org.opengis.feature.Property;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.AttributeType;
import org.opengis.feature.type.GeometryDescriptor;
import org.opengis.feature.type.Name;
import org.opengis.feature.type.PropertyDescriptor;
import org.opengis.filter.Filter;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.util.InternationalString;

import com.vividsolutions.jts.geom.Geometry;

/**
 * 
 * @author kengu
 *
 */
public class EFeatureInfo extends EStructureInfo<EFeatureFolderInfo> {
            
    /**
     * Mutable property: {@link #getSRID()} </p>
     * 
     * @see {@link #setSRID(String)} - invalidates all {@link EFeatureGeometryInfo#getDescriptor()
     *      geometry descriptors}
     * @see {@link EStructureInfo#addListener(EStructureInfoListener)} - listen for SRID changes.
     */
    public static final int SRID = 1;

    /**
     * Mutable property: {@link #eGetDefaultGeometryName()} </p>
     * 
     * @see {@link #eSetDefaultGeometryName(String)}
     * @see {@link EStructureInfo#addListener(EStructureInfoListener)} - listen for default geometry
     *      name changes.
     */
    public static final int DEFAULT_GEOMETRY_NAME = 2;
    
    /**
     * Static {@link EFeatureInfo#eUID} counter.
     */
    protected long eNexUID = 1;
    
    /**
     * Unique ID which enables efficient <i>{@link EFeatureInfo structure} 
     * equivalence</i> checking. <b>NOTE</b>: This equivalence is not
     * the same as <i>{@link Object#equals(Object) Object equivalence}</i>,
     * it only states that the structure is equal. Since the 
     * structure is immutable, a single unique value is enough to 
     * compare two structures for equivalence. Mutable fields like 
     * {@link #getSRID()} is not part of the structure. It can therefore 
     * not be assumed that if two structure have the same {@link #eUID}, then
     * the {@link #getSRID()} are also the same. 
     */
    public final long eUID;

    protected String eNsURI;

    protected String eDomainID;
    
    protected String eFolderName;

    protected String eClassName;

    protected String eReferenceName;

    protected String eGeometryListName;

    protected String srid = DEFAULT_SRID;

    protected CoordinateReferenceSystem crs;

    protected boolean eIsRoot = true;

    protected String eIDAttributeName;

    protected String eSRIDAttributeName;
    
    protected String eDefaultAttributeName;
    
    protected String eDefaultGeometryName;

    protected WeakReference<EClass> eClass;

    protected WeakReference<EClass> eParentClass;

    protected WeakReference<EReference> eReference;

    protected SimpleFeatureType featureType;

    /**
     * {@link EAttribute} id to {@link EFeatureAttributeInfo} instance {@link Map}
     */
    protected Map<String, EFeatureAttributeInfo> eAttributeInfoMap;

    /**
     * {@link EFeatureGeometry} id to {@link EFeatureGeometryInfo} instance {@link Map}
     */
    public Map<String, EFeatureGeometryInfo> eGeometryInfoMap;
    
    // ----------------------------------------------------- 
    //  Constructors
    // -----------------------------------------------------
    
    /**
     * Default constructor
     */
    protected EFeatureInfo() {
        eUID = eNexUID++;
    }
    
    /**
     * Structure copy constructor.
     * <p>
     * This method copies the structure into given context. 
     * The copied instance is {@link #isDangling() dangling} if the 
     * no parent structure was found in the new context.
     * </p>
     * <b>NOTE</b>: This method only adds a one-way reference from 
     * copied instance to given {@link EFeatureContext context}. 
     * No reference is added from the context to this feature. It does not
     * attach the {@link EFeatureInfo} instance with given 
     * {@link EFeatureContext context} {@link EFeatureContextInfo structure}, 
     * nor adds the {@link EClass#getEIDAttribute()} to the 
     * {@link EFeatureContext#eIDFactory()}. This must be done manually.
     * </p>  
     * @param eFeatureInfo - copy from this {@link EFeatureInfo} instance
     * @param eContextInfo - copy into this context
     * @see {@link EFeatureContextInfo#doAdapt(EFeatureInfo, boolean)}
     */
    protected EFeatureInfo(EFeatureInfo eFeatureInfo, EFeatureContextInfo eContextInfo) {
        //
        // Forward (copies context, state and hints)
        //
        super(eFeatureInfo, eContextInfo);        
        //
        // Mark as being structural equal to given structure
        //
        this.eUID = eFeatureInfo.eUID;
        //
        // Copy context path
        //
        this.eNsURI = eFeatureInfo.eNsURI;
        this.eDomainID = eFeatureInfo.eDomainID;
        this.eFolderName = eFeatureInfo.eFolderName;
        //
        // Copy EClass information
        //        
        this.eClassName = eFeatureInfo.eClassName;
        this.eClass = new WeakReference<EClass>(eFeatureInfo.eClass());
        //
        // Copy parent EClass information (folder is an EClass)
        //                
        this.eParentClass = new WeakReference<EClass>(eFeatureInfo.eParentClass());        
        this.eReferenceName = eFeatureInfo.eReferenceName;
        this.eReference = new WeakReference<EReference>(eFeatureInfo.eReference());
        //
        // Copy ID attribute information
        //                
        this.eIDAttributeName = eFeatureInfo.eIDAttributeName;
        //
        // Copy SRID attribute information
        //                
        this.eSRIDAttributeName = eFeatureInfo.eSRIDAttributeName;
        //
        // Copy default geometry attribute information
        //                
        this.eDefaultAttributeName = eFeatureInfo.eDefaultAttributeName;
        //
        // Copy other attributes 
        //
        this.isAvailable = eFeatureInfo.isAvailable;        
        //
        // Prepare to add attributes
        //
        this.eAttributeInfoMap = 
            new HashMap<String, EFeatureAttributeInfo>(eFeatureInfo.eAttributeInfoMap.size());
        this.eGeometryInfoMap = 
            new HashMap<String, EFeatureGeometryInfo>(eFeatureInfo.eGeometryInfoMap.size());
        //
        // Loop over all attributes and copy them
        //
        for(EFeatureAttributeInfo it : eFeatureInfo.eAttributeInfoMap.values()) {
            eAttributeInfoMap.put(it.eName,new EFeatureAttributeInfo(it,this));
        }
        for(EFeatureGeometryInfo it : eFeatureInfo.eGeometryInfoMap.values()) {
            eGeometryInfoMap.put(it.eName,new EFeatureGeometryInfo(it,this));
        }
    }
    
    
    // ----------------------------------------------------- 
    //  EFeatureInfo methods
    // -----------------------------------------------------
    
    @Override
    public boolean isAvailable() {
        //
        // Is available and has at least one geometry defined?
        //
        return super.isAvailable() && !eGeometryInfoMap.isEmpty();
    }

    /**
     * Check if {@link EFeature} is a root.
     * <p>
     * A {@link EFeature} roots are not referenced (contained).
     * </p>
     * 
     * @return <code>true</code> if EObject root.
     */
    public boolean isRoot() {
        return eIsRoot;
    }
        
    /**
     * Check if this not associated with any {@link #eParentInfo() parent structure}.
     * <p>
     * A dangling {@link EFeatureInfo} instance implies thatt this structure only exists 
     * in current context's EFeatureInfo {@link EFeatureContextInfo#eFeatureInfoCache() cache}.
     * </p>
     * Before this can be used 
     * @return <code>true</code> if dangling.
     */
    public boolean isDangling() {
        return eParentInfo(true)==null;
    }
            
    @Override
    public EFeatureFolderInfo eParentInfo() {
        //
        // Try to get structure
        //
        EFeatureFolderInfo eInfo = eParentInfo(true);
        //
        // Dangling?
        //
        if(eInfo==null) {
            throw new IllegalStateException(eName() + " is danling. No parent exists");
        }
        //
        // Finished
        //
        return eInfo;
    }

    /**
     * Get {@link Feature} name.
     * <p>
     * If this {@link #isRoot() is a root}, the {@link EClass} name is returned, else the name of
     * the {@link EObject#eContainmentFeature() containment reference} to this {@link EFeature} is
     * returned.
     * </p>
     * 
     * @return the {@link EFeature} name.
     */
    public String eName() {
        return eIsRoot ? eClassName : eReferenceName;
    }
        
    public String eNsURI() {
        return eNsURI;
    }

    public String eDomainID() {
        return eDomainID;
    }

    public String eFolderName() {
        return eFolderName;
    }

    /**
     * Get EFeature {@link EClass}
     */
    public EClass eClass() {
        return (eClass!=null ? eClass.get() : null);
    }

    /**
     * Get EFeature {@link EClass} name
     */
    public String eClassName() {
        return eClassName;
    }

    /**
     * Get name of {@link EReference} referencing this {@link EFeature}.
     * <p>
     * This is only set if the {@link EFeature} is not {@link #isRoot() a root}.
     * </p>
     */
    public EReference eReference() {
        return  (eReference!=null ? eReference.get() : null);
    }

    /**
     * Get name of {@link EReference} referencing this {@link EFeature}.
     * <p>
     * This is only set if the {@link EFeature} is not {@link #isRoot() a root}.
     * </p>
     */
    public String eReferenceName() {
        return eReferenceName;
    }

    /**
     * Get this {@link EClass parent} class.
     */
    public EClass eParentClass() {
        return eParentClass!=null ? eParentClass.get() : null;
    }

    /**
     * Get name of EFeature parent {@link EClass}
     */
    public String eParentClassName() {
        return eParentClass != null ? eParentClass().getName() : null;
    }
    
    /**
     * Get {@link EFeatureFolderInfo} instance.
     */
    @Override
    protected EFeatureFolderInfo eParentInfo(boolean checkIsValid) {
        EFeatureDataStoreInfo eInfo = eContext(checkIsValid).
            eStructure().eGetDataStoreInfo(eDomainID, eNsURI);
        if(eInfo!=null) {
            return eInfo.eGetFolderInfo(eFolderName);
        }
        return null;
    }
    
    /**
     * Get ID {@link EAttribute}
     * 
     * @return an {@link EAttribute} instance
     */
    public EAttribute eIDAttribute() {
        return getEAttribute(eIDAttributeName);
    }
    
    /**
     * Get SRID {@link EAttribute}
     * 
     * @return an {@link EAttribute} instance
     */
    public EAttribute eSRIDAttribute() {
        return getEAttribute(eSRIDAttributeName);
    }
    
    
    /**
     * Get default geometry {@link EAttribute}
     * 
     * @return an {@link EAttribute} instance
     */
    public EAttribute eDefaultAttribute() {
        return getEAttribute(eDefaultAttributeName);
    }
    
    /**
     * Get default {@link EFeatureGeometry} attribute.
     * <p>
     * 
     * @return a {@link EAttribute} instance, or <code>null</code> if no {@link EFeatureGeometry
     *         geometries} are defined.
     */
    public EAttribute eDefaultGeometry() {
        return getEAttribute(eGetDefaultGeometryName());
    }
    
    
    public String eGetDefaultGeometryName() {
        if (eDefaultGeometryName == null) {
            String eFirstFound = null;
            for (Entry<String, EFeatureGeometryInfo> it : eGeometryInfoMap.entrySet()) {
                if (eFirstFound == null) {
                    eFirstFound = it.getKey();
                }
                if (it.getValue().isDefaultGeometry) {
                    eDefaultGeometryName = it.getKey();
                    break;
                }
            }
            if (eDefaultGeometryName == null) {
                eGeometryInfoMap.get(eFirstFound).isDefaultGeometry = true;
                eDefaultGeometryName = eFirstFound;
            }
        }
        return eDefaultGeometryName;
    }

    public EFeatureGeometryInfo eSetDefaultGeometryName(String eNewName) {
        String eOldName = eGetDefaultGeometryName();
        if (!(eNewName == null || eNewName.length() == 0 && !eNewName.equals(eOldName))) {
            EFeatureGeometryInfo eOldInfo = eGeometryInfoMap.get(eOldName);
            EFeatureGeometryInfo eNewInfo = eGeometryInfoMap.get(eNewName);
            if (eNewInfo != null) {
                //
                // Update
                //
                eDefaultGeometryName = eNewName;
                //
                // Keep structures in-sync
                //
                if (eOldInfo != null) {
                    eOldInfo.setIsDefaultGeometry(false);
                }
                eNewInfo.setIsDefaultGeometry(true);
                //
                // Notify change of mutable property
                //
                fireOnChange(DEFAULT_GEOMETRY_NAME, eOldName, eNewName);
                //
                // Change completed
                //
                return eNewInfo;
            }
        }
        // No change
        //
        return null;
    }
    
    public EFeatureGeometryInfo eDefaultGeometryInfo() {
        return eGeometryInfoMap.get(eDefaultGeometryName);
    }    

    /**
     * Get spatial reference ID.
     * @return a SRID
     * @see {@link CRS} - utility class for converting SRIDs into a 
     * {@link CoordinateReferenceSystem} 
     */
    public String getSRID() {
        return srid;
    }

    /**
     * Set spatial reference ID.
     */
    public CoordinateReferenceSystem setSRID(String newSRID) throws IllegalArgumentException {
        final String oldSRID = getSRID();
        if (isAvailable() && !(newSRID == null || newSRID.length() == 0)
                && !newSRID.equals(oldSRID)) {
            // Update Spatial reference id
            //
            this.srid = newSRID;

            // Try create new CRS from SRID
            //
            try {
                CRSCache.decode(this, true);
            } catch (Exception e) {
                // Rollback to old SRID
                //
                srid = oldSRID;

                // Throw expected exception
                //
                throw new IllegalArgumentException("Failed to decode SRID " + "'" + srid + "'", e);
            }

            // Keep structures in-sync
            // (use this.srid since decodeCRS may enforce default value)
            //
            setSRID(oldSRID, this.srid, this.crs);

            // Change completed
            //
            return this.crs;

        }
        // No change
        //
        return null;
    }

    protected final void setSRID(String oldSRID, String newSRID, CoordinateReferenceSystem newCRS) {
        // Update Spatial reference id
        //
        this.srid = newSRID;

        // Keep structures in-sync
        //
        for (EFeatureGeometryInfo it : eGeometryInfoMap.values()) {
            it.setSRID(newSRID, newCRS);
        }

        // Notify listener of changes mutable property
        //
        fireOnChange(SRID, oldSRID, newSRID);

    }    

    public CoordinateReferenceSystem getCoordinateReferenceSystem() {
        return crs;
    }

    public SimpleFeatureType getFeatureType() {
        if (isAvailable() && featureType == null) {
            featureType = new SimpleFeatureDelegate();
        }
        return featureType;
    }

    public EAttribute getEAttribute(String eName) {
        EFeatureAttributeInfo eInfo = eGetAttributeInfo(eName, true);
        if (eInfo != null) {
            return eInfo.eAttribute();
        }
        return null;
    }

    public EFeatureAttributeInfo eGetAttributeInfo(String eName, boolean all) {
        EFeatureAttributeInfo eInfo = eAttributeInfoMap.get(eName);
        if (eInfo == null && all) {
            return eGetGeometryInfo(eName);
        }
        return eInfo;
    }

    public EFeatureGeometryInfo eGetGeometryInfo(String eName) {
        return eGeometryInfoMap.get(eName);
    }

    public boolean isGeometry(String eName) {
        return eGeometryInfoMap.containsKey(eName);
    }

    public boolean isDefaultGeometry(String eName) {
        return eDefaultGeometryName != null ? eDefaultGeometryName.equals(eName) : false;
    }

    public Map<String, EFeatureAttributeInfo> eGetAttributeInfoMap(boolean all) {
        Map<String, EFeatureAttributeInfo> map = new HashMap<String, EFeatureAttributeInfo>();
        map.putAll(eAttributeInfoMap);
        if (all) {
            map.putAll(eGeometryInfoMap);
        }
        return Collections.unmodifiableMap(map);
    }

    public Map<String, EAttribute> eGetAttributeMap(boolean all) {
        Map<String, EAttribute> eFoundMap = new HashMap<String, EAttribute>();
        Map<String, EAttribute> eAttrMap = EFeatureUtils.eGetAttributeMap(eClass());
        for (String eName : eAttrMap.keySet()) {
            if (eAttributeInfoMap.containsKey(eName) || all && eGeometryInfoMap.containsKey(eName)) {
                eFoundMap.put(eName, eAttrMap.get(eName));
            }
        }

        return Collections.unmodifiableMap(eFoundMap);
    }

    public Map<String, EAttribute> eGetAttributeMap(String[] eNames, boolean all) {
        Map<String, EAttribute> eFoundMap = new HashMap<String, EAttribute>();
        Map<String, EAttribute> eAttrMap = EFeatureUtils.eGetAttributeMap(eClass());
        for (String eName : eNames) {
            if (eAttrMap.containsKey(eName)
                    && (eAttributeInfoMap.containsKey(eName) || all
                            && eGeometryInfoMap.containsKey(eName))) {
                eFoundMap.put(eName, eAttrMap.get(eName));
            }
        }

        return Collections.unmodifiableMap(eFoundMap);
    }

    public Map<String, EFeatureGeometryInfo> eGetGeometryInfoMap() {
        return Collections.unmodifiableMap(eGeometryInfoMap);
    }
    
    /**
     * Validate given {@link EObject object} against this structure.
     * <p>
     * 
     * </p>
     * @param eObject - the {@link EObject} instance
     * 
     * @return <code>true</code> if valid, <code>false</code> otherwise.
     */
    public EFeatureStatus validate(EObject eObject) {
        //
        // Prepare
        //
        EClass eClass = eObject.eClass();
        EPackage ePackage = eClass.getEPackage();
        //
        // Get parent class?
        //
        EClass eParent = null; 
        if (!eIsRoot) {
            eParent = EFeatureUtils.eGetContainingClass(eObject);
        }
        //
        // Forward
        //
        return validate(ePackage,eParent);
    }

    /**
     * Validate {@link EFeature} structure.
     * <p>
     * If this {@link EFeature} is not {@link #isRoot() a root}, given {@link EClass} instance is
     * validated against the {@link #eReference() reference name} to ensure that such a reference
     * exist.
     * </p>
     * @param ePackage - given {@link EPackage} instance
     * @param eParent - {@link EClass} of given {@link EFeature} parent instance
     * @return <code>true</code> if valid, <code>false</code> otherwise.
     */
    public EFeatureStatus validate(EPackage ePackage, EClass eParent) {
        //
        // Initialize
        //
        EFeatureStatus s;
        //
        // Invalidate structure
        //
        doInvalidate(false);
        //
        // 1) Validate reference?
        //
        EReference eReference = null;
        if (!eIsRoot) {
            if (eParent == null) {
                return failure(this,
                        eName(), "Feature mismatch: Flagged as !eIsRoot() but parent class is null");
            }
            eReference = EFeatureUtils.eGetReference(eParent, this.eReferenceName);
            if (eReference == null) {
                return failure(this, eName(), "Feature mismatch: EReference " + this.eReferenceName
                        + " not found");
            }
        }
        //
        // 2) Validate EClass name
        //
        EClassifier eClassifier = ePackage.getEClassifier(eClassName);
        if (!(eClassifier instanceof EClass)) {
            return failure(this, eName(), "Feature mismatch: EClass " + eClassName + " not found");
        }
        EClass eClass = ((EClass) eClassifier);
        //
        // Get list of all EAttributes
        //
        Map<String, EAttribute> eAttrMap = EFeatureUtils.eGetAttributeMap(eClass);
        //
        // 3) Validate ID attribute
        //
        EAttribute eAttribute = eClass.getEIDAttribute();
        if (eAttribute != null) {
            //
            // Get attribute name
            //
            String eName = eAttribute.getName();
            //
            // Get EAttribute ID instance
            //
            EFeatureAttributeInfo eInfo = eGetAttributeInfo(eName, true);
            //
            // Validate attribute
            //
            if(!(s = eInfo.validate(true, eAttribute)).isSuccess()) {
                //
                // Given EClass specified ID attribute was not a valid
                //
                return s;
            }
        } else if (!eAttrMap.containsKey(eIDAttributeName)) {
            return failure(this, eName(), "Feature mismatch: ID EAttribute '" + eIDAttributeName + "' not found");
        }
        //
        // EClass specified ID attribute is already validated
        //
        eAttrMap.remove(eAttribute);
        //
        // 4) Validate attributes
        //
        for (EFeatureAttributeInfo it : eAttributeInfoMap.values()) {
            eAttribute = eAttrMap.get(it.eName);
            if (eAttribute == null) {
                return failure(this, eName(), "Feature mismatch: EAttribute " + it.eName
                        + " not found in EClass");
            }
            boolean isID = eAttribute.getName().equals(eIDAttributeName);
            if (!(s = it.validate(isID, eAttribute)).isSuccess()) {
                return s;
            }
        }
        //
        // 5) Validate geometries
        //
        for (EFeatureGeometryInfo it : eGeometryInfoMap.values()) {
            eAttribute = eAttrMap.get(it.eName);
            if (eAttribute == null) {
                return failure(this, eName(), "Feature mismatch: EGeometry " + it.eName
                        + " not found in EClass");
            }
            boolean isID = eAttribute.getName().equals(eIDAttributeName);
            if (!(s = it.validate(isID, eAttribute)).isSuccess()) {
                return s;
            }
        }
        //
        // Store valid state
        //
        this.isValid = true;
        //
        // Store as weak references. This prevents memory leakage
        // when doDispose() is not called explicitly.
        //
        this.eParentClass = new WeakReference<EClass>(eParent);
        this.eReference = new WeakReference<EReference>(eReference);

        // Confirm that structure is valid
        //
        return structureIsValid(eName());
    }

    /**
     * Validate feature against this structure.
     * 
     * @param feature - feature to be validated
     * @return <code>true</code> if valid.
     */
    public EFeatureStatus validate(Feature feature) {
        //
        // TODO: Does this work?
        //
        if (!getFeatureType().equals(feature.getType())) {
            return featureIsInvalid();
        }
        // Confirm that feature is valid
        //
        return featureIsValid();
    }

    // ----------------------------------------------------- 
    //  EStructureInfo implementation
    // -----------------------------------------------------
    
    @Override
    protected void doInvalidate(boolean deep) {
        //
        // Dispose weak references
        //
        this.eParentClass = null;
        this.eReference = null;
        //
        // Do a deep invalidation?
        //
        if (deep) {
            for (EStructureInfo<?> it : eGetAttributeInfoMap(true).values()) {
                it.doInvalidate(true);
            }
        }
        
    }
    
    @Override
    protected void doAdapt() {        
        // 
        // Update ALL attributes (includes geometry structures)
        // 
        for(EFeatureAttributeInfo it : eGetAttributeInfoMap(true).values()) {
            it.eAdapt(this);
        }        
    }

    @Override
    protected void doDispose() {
        this.eClass = null;
        this.eParentClass = null;
        this.eReference = null;
        for (EFeatureAttributeInfo it : eGetAttributeInfoMap(true).values()) {
            it.dispose();
        }
        eAttributeInfoMap.clear();
        eAttributeInfoMap = null;
        eGeometryInfoMap.clear();
        eGeometryInfoMap = null;
        featureType = null;
        crs = null;
    }

    // ----------------------------------------------------- 
    //  Public construction methods
    // -----------------------------------------------------
    
    /**
     * Create EFeature {@link EFeatureInfo structure} from 
     * given EMF {@link EObject object} when the context is known.
     * </p>
     * @param eContext - {@link EFeatureContext context} of given object
     * @param eFeature - {@link EFeature} or EFeature compatible EMF {@link EObject object}. 
     * @param eHints - {@link EFeatureInfo} construction hints (ID attribute name etc.)
     * @see {@link EFeatureHints}
     */
    public static EFeatureInfo create(EFeatureContext eContext, EObject eFeature, EFeatureHints eHints) {
        //
        // Get structure information
        //
        EClass eClass = eFeature.eClass();
        String eNsURI = EFeatureUtils.eGetNsURI(eClass);        
        //
        // Check cache first
        //
        EFeatureInfo eInfo = eContext.eStructure().
            eFeatureInfoCache().get(null, null, eClass);
        //
        // Does not exist?
        //
        if(eInfo == null) {
            //
            // Create new instance
            //
            eInfo = new EFeatureInfo();
            //
            // Set construction hints
            //
            eInfo.eHints = (eHints != null ? eHints : new EFeatureHints());
            //
            // Set context
            //
            eInfo.eContextID = eContext.eContextID();
            eInfo.eContext = new WeakReference<EFeatureContext>(eContext);
            eInfo.eFactory = new WeakReference<EFeatureContextFactory>(eContext.eContextFactory());
            //
            // Defined the structure
            //
            eInfo = define(eInfo, eContext, eNsURI, null, null, eClass);
        }
        //
        // Finished
        //
        return eInfo;
    }

    // ----------------------------------------------------- 
    //  Static Helper methods
    // -----------------------------------------------------
    
    /**
     * Create EFeature {@link EFeatureInfo structure} from 
     * given EMF {@link EClass class} in given {@link EFeatureFolderInfo folder}.
     * <p>
     * <b>NOTE</b>: This method attaches the {@link EFeatureInfo} instance 
     * with given {@link EFeatureContext context} {@link EFeatureContextInfo structure}, 
     * and adds the {@link EClass#getEIDAttribute()} to the 
     * {@link EFeatureContext#eIDFactory()}.
     * </p>
     * @param eFolderInfo - the {@link EFeatureFolderInfo folder} instance 
     * @param eClass - {@link EFeature} or EFeature compatible EMF {@link EClass class} . 
     * @return a new {@link EFeatureInfo} instance if not defined, 
     * or the existing structure if defined
     */
    protected static EFeatureInfo create(
            EFeatureFolderInfo eFolderInfo, EClass eClass) {
        //
        // Get structure information
        //
        String eNsURI = eFolderInfo.eNsURI;
        String eDomainID = eFolderInfo.eDomainID;
        String eFolderName = eFolderInfo.eName;
        //
        // Get context information
        //
        EFeatureContext eContext = eFolderInfo.eContext(false);
        //
        // Check cache first
        //
        EFeatureInfo eInfo = eContext.eStructure().
            eFeatureInfoCache().get(eDomainID, eFolderName, eClass);
        //
        // Was not cached?
        //
        if (eInfo == null) {
            //
            // Create new instance
            //
            eInfo = new EFeatureInfo();
            //
            // Set construction hints
            //
            eInfo.eHints = (eFolderInfo.eHints != null ? eFolderInfo.eHints : new EFeatureHints());
            //
            // Set context
            //
            eInfo.eFactory = eFolderInfo.eFactory;
            eInfo.eContext = eFolderInfo.eContext;
            eInfo.eContextID = eContext.eContextID();
            //
            // Forward
            //
            eInfo = define(eInfo, eContext, eNsURI, eDomainID, eFolderName, eClass);
        }
        //
        // Finished
        //
        return eInfo;
    }
    
    /**
     * Define EFeature {@link EFeatureInfo structure} from given information.
     * <p>
     * <b>NOTE</b>: This method assumes that the {@link EFeatureContext} and 
     * {@link EFeatureHints} are already set. This method also attaches the 
     * {@link EFeatureInfo} instance with given context 
     * {@link EFeatureContextInfo structure}, and adds the 
     * {@link EClass#getEIDAttribute()} to the {@link EFeatureContext#eIDFactory()}.
     * </p>
     * @param eInfo
     * @param eContext
     * @param eNsURI
     * @param eDomainID
     * @param eFolderName - the {@link EFeatureFolderInfo folder} instance 
     * @param eClass - {@link EFeature} or EFeature compatible EMF {@link EClass class} . 
     * @return a {@link EFeatureInfo} instance
     */
    private static EFeatureInfo define(
            EFeatureInfo eInfo, EFeatureContext eContext, 
            String eNsURI, String eDomainID, String eFolderName, EClass eClass) {
        //
        // Set context path
        //        
        eInfo.eNsURI = eNsURI;
        eInfo.eDomainID = eDomainID;
        eInfo.eFolderName = eFolderName;
        //
        // Set EClass implementing EFeature 
        //                
        eInfo.eClassName = eClass.getName();
        eInfo.eClass = new WeakReference<EClass>(eClass);        
        //
        // Set ID attribute 
        //                
        EAttribute eIDAttribute = eGetIDAttribute(eClass,eInfo.eHints);
        eInfo.eIDAttributeName = eIDAttribute.getName();
        //
        // Set SRID attribute 
        //                
        EAttribute eSRIDAttribute = eGetSRIDAttribute(eClass, eInfo.eHints);
        eInfo.eSRIDAttributeName = eSRIDAttribute.getName();
        //
        // Set default geometry attribute 
        //                
        EAttribute eDefaultAttribute = eGetDefaultAttribute(eClass, eInfo.eHints);
        eInfo.eDefaultAttributeName = eDefaultAttribute.getName();
        //
        // Create EFeature attribute structures 
        //                        
        EList<EAttribute> eAttributes = eClass.getEAllAttributes();
        eInfo.eAttributeInfoMap = attributes(eInfo, eAttributes);
        eInfo.eGeometryInfoMap = geometries(eInfo, eAttributes);
        //
        // Register ID attribute with ID factory
        //
        eContext.eIDFactory().add(eClass,eIDAttribute);
        //
        // Add to cache
        //
        eContext.eStructure().doAttach(eInfo);
        //
        // Finished
        //
        return eInfo;
    }    
    
    protected static EAttribute eGetIDAttribute(EClass eClass, EFeatureHints eHints) {
        EAttribute eIDAttribute = eGetAttribute(eClass, eHints, EFeatureHints.EFEATURE_ID_ATTRIBUTE_HINTS);
        if(eIDAttribute == null) {
            eIDAttribute = eClass.getEIDAttribute();            
        }
        return eIDAttribute;
    }
    
    protected static EAttribute eGetSRIDAttribute(EClass eClass, EFeatureHints eHints) {
        EAttribute eIDAttribute = eGetAttribute(eClass, eHints, EFeatureHints.EFEATURE_SRID_ATTRIBUTE_HINTS);
        if(eIDAttribute == null) {
            eIDAttribute = EFeaturePackage.eINSTANCE.getEFeature_SRID();            
        }
        return eIDAttribute;
    }
    
    protected static EAttribute eGetDefaultAttribute(EClass eClass, EFeatureHints eHints) {
        EAttribute eIDAttribute = eGetAttribute(eClass, eHints, EFeatureHints.EFEATURE_DEFAULT_ATTRIBUTE_HINTS);
        if(eIDAttribute == null) {
            eIDAttribute = EFeaturePackage.eINSTANCE.getEFeature_Default();            
        }
        return eIDAttribute;
    }
    
    protected static EAttribute eGetAttribute(EClass eClass, EFeatureHints eHints, int eHint) {
        EAttribute eIDAttribute = null;
        if(eHints!=null) {
            eIDAttribute = eHints.eGetAttribute(eClass, eHint);
        } 
        return eIDAttribute;
    }
    
    protected static String eGetSRID(EFeatureHints eHints) {
        Object eSRID = null;
        if(eHints!=null) {
            eSRID = eHints.get(EFeatureHints.EFEATURE_DEFAULT_SRID_HINT);
        }
        return eSRID !=null ? eSRID.toString() : EFeatureConstants.DEFAULT_SRID;
    }

    protected static String eGetDefaultGeometryName(EFeatureHints eHints) {
        Object eDefaultName = null;
        if(eHints!=null) {
            eDefaultName = eHints.get(EFeatureHints.EFEATURE_DEFAULT_GEOMETRY_NAME_HINT);
        }
        return eDefaultName !=null ? eDefaultName.toString() : EFeatureConstants.DEFAULT_GEOMETRY_NAME;
    }    
    
    protected static Map<String, EFeatureAttributeInfo> attributes(EFeatureInfo eFeatureInfo,
            EList<EAttribute> eAttributes) {
        //
        // Prepare
        //
        Map<String, EFeatureAttributeInfo> eAttributeMap = 
            new HashMap<String, EFeatureAttributeInfo>(eAttributes.size());
        //
        // Get name of EAttribute ID
        //
        String eID = eFeatureInfo.eIDAttributeName;
        //
        // Inspect EMF attributes, adding all with non geometry values. 
        //
        for (EAttribute it : eAttributes) {
            //
            // Does NOT contain geometry data?
            //
            if (!Geometry.class.isAssignableFrom(it.getEAttributeType().getInstanceClass())) {
                String eName = it.getName();
                eAttributeMap.put(eName, EFeatureAttributeInfo.create(
                        eFeatureInfo, eName.equals(eID), it));
            }
        }
        return Collections.unmodifiableMap(eAttributeMap);
    }

    protected static Map<String, EFeatureGeometryInfo> geometries(EFeatureInfo eFeatureInfo,
            EList<EAttribute> eAttributes) {
        //
        // Prepare
        //        
        String eDefault = null;
        String srid = eFeatureInfo.srid;
        CoordinateReferenceSystem crs = eFeatureInfo.crs;
        Map<String, EFeatureGeometryInfo> eGeometryMap = 
            new HashMap<String, EFeatureGeometryInfo>(eAttributes.size());
        //
        // Inspect EMF attributes, adding all with geometry values. 
        //                
        for (EAttribute it : eAttributes) {
            //
            // Contains geometry data?
            //
            if (Geometry.class.isAssignableFrom(it.getEAttributeType().getInstanceClass())) {
                //
                // Initialize default geometry name?
                //
                String eName = it.getName();
                if (eDefault == null) {
                    eDefault = eName;
                }
                //
                // Create structure instance
                //
                EFeatureGeometryInfo eGeometryInfo = 
                    EFeatureGeometryInfo.create(eFeatureInfo, 
                            eName.equals(eDefault), srid, crs, it);
                //
                // Add to cache
                //
                eGeometryMap.put(eName, eGeometryInfo);
            }
        }
        return Collections.unmodifiableMap(eGeometryMap);
    }

    protected final EFeatureStatus featureIsValid() {
        return success("Feature is valid: " + getClass().getSimpleName() + " [" + eName() + "]");
    }

    protected final EFeatureStatus featureIsInvalid() {
        return failure(this, eName(), "Feature is invalid: " + getClass().getSimpleName() 
                + " [" + eName() + "]");
    }
    
    // ----------------------------------------------------- 
    //  SimpleFeatureDescriptor implementation
    // -----------------------------------------------------

    private class SimpleFeatureDelegate implements SimpleFeatureType {
        private Map<String, Integer> index;

        private Map<Object, Object> userData;

        private Collection<PropertyDescriptor> descriptors;

        private Class<?> binding = Collection.class;

        public SimpleFeatureDelegate() {
            index = buildIndex(this);
        }

        public boolean isIdentified() {
            // Features are always identified
            //
            return true;
        }

        public Name getName() {
            return new NameImpl(EFeatureInfo.this.eName());
        }

        public boolean isAbstract() {
            return false;
        }

        public GeometryDescriptor getGeometryDescriptor() {
            EFeatureGeometryInfo eInfo = eGeometryInfoMap.get(eDefaultGeometry().getName());
            return eInfo != null ? eInfo.getDescriptor() : null;
        }

        public CoordinateReferenceSystem getCoordinateReferenceSystem() {
            return crs;
        }

        @SuppressWarnings("unchecked")
        public Class<Collection<Property>> getBinding() {
            return (Class<Collection<Property>>) binding;
        }

        public Collection<PropertyDescriptor> getDescriptors() {
            if(descriptors==null) {
                descriptors = new HashSet<PropertyDescriptor>();
                for (EFeatureAttributeInfo it : eGetAttributeInfoMap(true).values()) {
                    descriptors.add(it.getDescriptor());
                }
                descriptors = Collections.unmodifiableList(new ArrayList<PropertyDescriptor>(descriptors));
            }
            return descriptors;
        }

        public boolean isInline() {
            // Apparently not in use, return false
            //
            return false;
        }

        public AttributeType getSuper() {
            return null;
        }

        public List<Filter> getRestrictions() {
            return Collections.emptyList();
        }

        public InternationalString getDescription() {
            return null;
        }

        public Map<Object, Object> getUserData() {
            if (userData == null) {
                userData = new HashMap<Object, Object>();
            }
            return userData;
        }

        public String getTypeName() {
            return getName().getLocalPart();
        }

        @SuppressWarnings({ "rawtypes", "unchecked" })
        public List<AttributeDescriptor> getAttributeDescriptors() {
            return (List) getDescriptors();
        }

        public List<AttributeType> getTypes() {
            synchronized (this) {
                List<AttributeType> types = new ArrayList<AttributeType>();
                for (EFeatureAttributeInfo it : eGetAttributeInfoMap(true).values()) {
                    types.add(it.getDescriptor().getType());
                }
                return types;
            }

        }

        public AttributeType getType(Name name) {
            AttributeDescriptor attribute = getDescriptor(name);
            if (attribute != null) {
                return attribute.getType();
            }
            return null;
        }

        public AttributeType getType(String name) {
            AttributeDescriptor attribute = getDescriptor(name);
            if (attribute != null) {
                return attribute.getType();
            }
            return null;
        }

        public AttributeType getType(int index) {
            return getTypes().get(index);
        }

        public AttributeDescriptor getDescriptor(Name eName) {
            return getDescriptor(eName.getURI());
        }

        public AttributeDescriptor getDescriptor(String eName) {
            EFeatureAttributeInfo eInfo = eGetAttributeInfo(eName, true);
            return (eInfo != null ? eInfo.getDescriptor() : null);
        }

        public AttributeDescriptor getDescriptor(int index) {
            return getAttributeDescriptors().get(index);
        }

        public int indexOf(Name name) {
            if (name.getNamespaceURI() == null) {
                return indexOf(name.getLocalPart());
            }
            // otherwise do a full scan
            int index = 0;
            for (AttributeDescriptor descriptor : getAttributeDescriptors()) {
                if (descriptor.getName().equals(name)) {
                    return index;
                }
                index++;
            }
            return -1;
        }

        public int indexOf(String name) {
            Integer idx = index.get(name);
            if (idx != null) {
                return idx.intValue();
            } else {
                return -1;
            }
        }

        public int getAttributeCount() {
            return getAttributeDescriptors().size();
        }
        
        /**
         * Builds the name -> position index used by simple features for fast attribute lookup
         * 
         * @param featureType
         * @return
         */
        private Map<String, Integer> buildIndex(SimpleFeatureDelegate featureType) {
            // build an index of attribute name to index
            Map<String, Integer> index = new HashMap<String, Integer>();
            int i = 0;
            for (AttributeDescriptor ad : featureType.getAttributeDescriptors()) {
                index.put(ad.getLocalName(), i++);
            }
            if (featureType.getGeometryDescriptor() != null) {
                index.put(null, index.get(featureType.getGeometryDescriptor().getLocalName()));
            }
            return index;
        }
        
        

    }    

}