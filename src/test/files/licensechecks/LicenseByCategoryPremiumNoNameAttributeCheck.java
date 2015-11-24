/**
 * This file is the sample code against we run our unit test.
 * It is placed src/test/files in order to not be part of the maven compilation.
 **/
import org.mule.api.annotations.Connector;
import org.mule.api.annotations.Processor;
import org.mule.api.annotations.licensing.RequiresEnterpriseLicense;
import org.mule.api.annotations.licensing.RequiresEntitlement;

@Connector
@RequiresEnterpriseLicense
@RequiresEntitlement
class LicenseByCategoryPremiumNoNameAttributeCheck {

    @Processor
    public void aMethod() {
    }

}