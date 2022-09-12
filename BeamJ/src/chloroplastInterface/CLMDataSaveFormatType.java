package chloroplastInterface;

import javax.swing.filechooser.FileNameExtensionFilter;

public class CLMDataSaveFormatType implements SaveFormatType<PhotometricResource>
{
    private static final String DESCRIPTION = "Chloroplast movement binary format (.clm)";
    private static final String EXTENSION = "clm";
    private static final FileNameExtensionFilter FILTER = new FileNameExtensionFilter(DESCRIPTION,EXTENSION);

    private static final CLMDataSaveFormatType INSTANCE = new CLMDataSaveFormatType();

    private CLMDataSaveFormatType(){}; 

    public static CLMDataSaveFormatType getInstance()
    {
        return INSTANCE;
    }

    @Override
    public Saver<PhotometricResource> getSaver()
    {
        return CLMSaver.getInstance();
    }

    @Override
    public String getDescription() 
    {
        return DESCRIPTION;
    }

    @Override
    public String getExtension() 
    {
        return EXTENSION;
    }

    @Override
    public FileNameExtensionFilter getFileNameExtensionFilter() 
    {
        return FILTER;
    }
}
