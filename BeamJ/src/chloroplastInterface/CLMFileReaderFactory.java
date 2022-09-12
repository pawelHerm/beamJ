package chloroplastInterface;

import atomicJ.readers.SourceReaderFactory;

public class CLMFileReaderFactory extends SourceReaderFactory<CLMFileReader>
{
    private static final CLMFileReaderFactory INSTANCE = new CLMFileReaderFactory(){};

    private CLMFileReaderFactory(){};

    public static CLMFileReaderFactory getInstance()
    {
        return INSTANCE;
    }

    @Override
    public CLMFileReader getReader() 
    {
        return CLMFileReader.getInstance();
    }

    @Override
    public String getDescription()
    {
        return CLMFileReader.getDescription();
    }

    @Override
    public String[] getAcceptedExtensions()
    {
        return CLMFileReader.getAcceptedExtensions();
    }
}
