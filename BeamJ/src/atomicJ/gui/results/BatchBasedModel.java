package atomicJ.gui.results;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.jfree.util.ObjectUtilities;

import atomicJ.analysis.Processed1DPack;
import atomicJ.gui.AbstractModel;
import atomicJ.sources.Channel1DSource;

public abstract class BatchBasedModel <S extends Channel1DSource<?>, E extends Processed1DPack<E,S>> extends AbstractModel implements ResultDataListener<S,E>, PropertyChangeListener
{
    public static final String BATCH_IDENTITY_ALL = "All";

    public static final String RESTRICT_TO_SELECTED_PACKS = "RestrictToSelectedPacks";
    public static final String AVAILABLE_BATCH_IDENTITIES = "AvailableBatchIdentities";
    public static final String BATCH_IDENTITY = "BatchIdentity";
    public static final String RESULTS_AVAILABLE = "ResultsAvailable";
    public static final String SELECTED_PACKS_AVAILABLE = "SelectedPacksAvailable";
    public static final String APPLY_ENABLED = "ApplyEnabled";

    private boolean restrictToSelectedPacks;

    private List<String> availableBatchIdentities = Collections.singletonList(BATCH_IDENTITY_ALL);
    private String selectedBatchIdentity = null;

    private boolean selectedPacksAvailable;
    private boolean resultsAvailable;

    private final ResultDataModel<S,E> dataModel;

    private boolean applied;

    public BatchBasedModel(ResultDataModel<S,E> dataModel)
    {     
        this.dataModel = dataModel;
        this.availableBatchIdentities = calculateAvailableBatchIdentities();
        this.selectedBatchIdentity = BATCH_IDENTITY_ALL;

        this.selectedPacksAvailable = dataModel.getResultSelectionModel().isSelectedPacksAvailable();
        this.resultsAvailable = !dataModel.isEmpty();
        this.dataModel.getResultSelectionModel().addPropertyChangeListener(this);
        this.dataModel.addResultModelListener(this);
    }

    public boolean isRestrictToSelection() {
        return restrictToSelectedPacks;
    }

    public void setRestrictToSelection(boolean restrictToSelectedPacksNew) {

        if(this.restrictToSelectedPacks != restrictToSelectedPacksNew)
        {
            if(!selectedPacksAvailable && restrictToSelectedPacksNew)
            {
                //this firing of property change may seem artificial, as the model property
                //did not really changed. However, it is necessary, as it may be the case
                //that setRestrictToSelectedPacks() was called by a Swing component,
                //whose state changed immediately upon user's action (ex. mouse click)
                //then the model must enforce reversion of its state by calling this 'artificial'
                //event. In this way we prevent inconsistency between the model and the user's interface

                firePropertyChange(RESTRICT_TO_SELECTED_PACKS, restrictToSelectedPacksNew, this.restrictToSelectedPacks);
                return;
            }

            boolean restrictToSelectedPacksOld = this.restrictToSelectedPacks;
            this.restrictToSelectedPacks = restrictToSelectedPacksNew;

            handleChangeSelectionRestriction();
            checkIfAvailableBatchIdsChanged();

            firePropertyChange(RESTRICT_TO_SELECTED_PACKS, restrictToSelectedPacksOld, restrictToSelectedPacksNew);
        }
    }

    protected void handleChangeSelectionRestriction() {}

    public String getSelectedBatchIdentity()
    {
        return selectedBatchIdentity;
    }

    public void setSelectedBatchIdentity(String identityNew) {

        if(!ObjectUtilities.equal(this.selectedBatchIdentity, identityNew))
        {
            if(!availableBatchIdentities.contains(identityNew))
            {
                //this firing of property change may seem artificial, as the model property
                //did not really changed. However, it is necessary, as it may be the case
                //that setSelectedBatchIdentity() was called by a Swing component,
                //whose state changed immediately upon user's action (ex. mouse click)
                //then the model must enforce reversion of its state by calling this 'artificial'
                //event. In this way we prevent inconsistency between the model and the user's interface

                firePropertyChange(BATCH_IDENTITY, identityNew, this.selectedBatchIdentity);

                return;
            }

            String identityOld = this.selectedBatchIdentity;
            this.selectedBatchIdentity = identityNew;

            handleChangeOfSelectedBatchIdentity();

            firePropertyChange(BATCH_IDENTITY, identityOld, identityNew);
        }
    }

    protected void handleChangeOfSelectedBatchIdentity(){};



    public List<String> getAvailableBatchIdentities()
    {
        return availableBatchIdentities;
    }

    private void checkIfAvailableBatchIdsChanged()
    {
        List<String> availableBatchIdentitiesNew = calculateAvailableBatchIdentities();     
        List<String> availableBatchIdentitiesOld = this.availableBatchIdentities;
        this.availableBatchIdentities = availableBatchIdentitiesNew;

        firePropertyChange(AVAILABLE_BATCH_IDENTITIES, availableBatchIdentitiesOld, new ArrayList<>(availableBatchIdentitiesNew));

        refreshSelectedBatchIdentity();
    }

    private void refreshSelectedBatchIdentity()
    {
        if(selectedBatchIdentity != null)
        {
            if(!availableBatchIdentities.contains(selectedBatchIdentity))
            {
                setSelectedBatchIdentity(BATCH_IDENTITY_ALL);
            }
        }
    }

    private List<String> calculateAvailableBatchIdentities()
    {
        List<String> batchIdentities = new ArrayList<>();
        batchIdentities.add(BATCH_IDENTITY_ALL);

        batchIdentities.addAll(dataModel.getAvailableBatchIds(restrictToSelectedPacks));

        return batchIdentities;
    }

    public boolean isSelectedPacksAvailable()
    {
        return selectedPacksAvailable;
    }

    private void checkIfSelectedPacksAvailableChanged()
    {
        boolean selectedPacksAvailableNew = dataModel.getResultSelectionModel().isSelectedPacksAvailable();

        boolean selectedPacksAvailableOld = this.selectedPacksAvailable;
        this.selectedPacksAvailable = selectedPacksAvailableNew;

        firePropertyChange(SELECTED_PACKS_AVAILABLE, selectedPacksAvailableOld, selectedPacksAvailableNew);

        if(!selectedPacksAvailable)
        {
            setRestrictToSelection(false);
        }
    }

    public boolean isResultsAvailable()
    {
        return resultsAvailable;
    }

    private void checkIfResultsAvailableChanged()
    {
        boolean resultsAvailableNew = !dataModel.isEmpty();

        boolean resultsAvailableOld = this.resultsAvailable;
        this.resultsAvailable = resultsAvailableNew;

        firePropertyChange(RESULTS_AVAILABLE, resultsAvailableOld, resultsAvailableNew);
    }

    public List<E> getPacks()
    {
        if(BATCH_IDENTITY_ALL.equals(this.selectedBatchIdentity))
        {
            return dataModel.getProcessedPacks(restrictToSelectedPacks);
        }
        return dataModel.getProcessedPacks(selectedBatchIdentity, restrictToSelectedPacks);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt)
    {
        String property = evt.getPropertyName();

        if(ResultSelectionModel.BATCHES_WITH_SELECTED_PACKS.equals(property))
        {
            checkIfAvailableBatchIdsChanged();
        }
        else if(ResultSelectionModel.SELECTED_PACKS_AVAILABLE.equals(property))
        {
            checkIfSelectedPacksAvailableChanged();
        }
    }

    public abstract void reset();

    public boolean isApplied()
    {
        return applied;
    }

    public void apply()
    {
        this.applied = true;
    }

    public boolean isApplyEnabled() {
        return true;
    }

    @Override
    public void batchesCleared(ResultDataEvent<S,E> event)
    {
        checkIfAvailableBatchIdsChanged();
        checkIfResultsAvailableChanged();
    }

    @Override
    public void batchesAdded(ResultDataEvent<S,E> event) 
    {
        checkIfAvailableBatchIdsChanged();      
        checkIfResultsAvailableChanged();
    }

    @Override
    public void batchesRemoved(ResultDataEvent<S,E> event) 
    {
        checkIfAvailableBatchIdsChanged();      
        checkIfResultsAvailableChanged();

    }

    @Override
    public void packsAdded(ResultDataEvent<S,E> event) {
    }

    @Override
    public void packsRemoved(ResultDataEvent<S,E> event) {
    }
}
