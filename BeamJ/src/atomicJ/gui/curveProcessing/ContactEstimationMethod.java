package atomicJ.gui.curveProcessing;

public enum ContactEstimationMethod 
{
    CONTACT_MODEL_BASED("Based on contact model")
    {
        @Override
        public boolean isPostcontactDegreeInputRequired() 
        {
            return false;
        }
    }, 
    CONTACT_MODEL_INDEPENDENT("Model independent") 
    {
        @Override
        public boolean isPostcontactDegreeInputRequired() 
        {
            return true;
        }
    };

    private final String name;

    ContactEstimationMethod(String name)
    {
        this.name = name;
    }

    public abstract boolean isPostcontactDegreeInputRequired();

    public static ContactEstimationMethod getValue(String identifier)
    {
        return getValue(identifier, null);
    }

    public static ContactEstimationMethod getValue(String identifier, ContactEstimationMethod fallBackValue)
    {
        ContactEstimationMethod estimator = fallBackValue;

        if(identifier != null)
        {
            for(ContactEstimationMethod est : ContactEstimationMethod.values())
            {
                String estIdentifier = est.getIdentifier();
                if(estIdentifier.equals(identifier))
                {
                    estimator = est;
                    break;
                }
            }
        }

        return estimator;
    }


    @Override
    public String toString()
    {
        return name;
    }

    public String getIdentifier()
    {
        return name();
    }
}
