package chloroplastInterface;

import javax.swing.filechooser.FileNameExtensionFilter;

public interface SaveFormatType<S>
{
    public Saver<S> getSaver();
    public String getDescription();
    public String getExtension();
    public FileNameExtensionFilter getFileNameExtensionFilter();
}
