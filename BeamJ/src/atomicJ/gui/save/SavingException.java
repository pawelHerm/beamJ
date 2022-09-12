package atomicJ.gui.save;

public class SavingException extends Exception
{
    private static final long serialVersionUID = 1L;

    public SavingException(String message)
    {
        super(message);
    }

    public SavingException(String message, Throwable original)
    {
        super(message, original);
    }
}
