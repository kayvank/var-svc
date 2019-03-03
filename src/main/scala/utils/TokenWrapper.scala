package utils

import com.vevo.tokens.TokenKey
import utils.Global.cfgVevo

object TokenWrapper {

  lazy val tokenKey = new TokenKey(
    cfgVevo.getString("castle.key"),
    cfgVevo.getString("castle.derivedKey"),
    cfgVevo.getString("castle.validationKey"))
}
