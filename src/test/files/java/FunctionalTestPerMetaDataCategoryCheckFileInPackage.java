import org.mule.api.annotations.Connector;
import org.mule.api.annotations.Processor;
import org.mule.api.annotations.components.MetaDataCategory;
import org.mule.api.annotations.MetaDataKeyRetriever;
import org.mule.api.annotations.MetaDataRetriever;

@MetaDataCategory
public class CreateLeadFieldsMetaDataInPackage {

    @MetaDataKeyRetriever
    public List<MetaDataKey> getMetaDataKeys() throws SomeException {
        return null;
    }

    @MetaDataRetriever
    public MetaData getMetaData(final MetaDataKey key) throws SomeException {
        return null;
    }

}