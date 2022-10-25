/**
 * README
 * This extension is being triggered by OIS100/PACHK/PRE
 *
 * Name: CUS_xt_OIS100_A_Chk_Pre_CheckFACI
 * Description: XTend made to forbid the user from choosing a Facility that is not in their DIVI
 * Date       Changed By                     Description
 * 20220601   Ludovic TRAVERS                Create CUS_xt_OIS100_A_Chk_Pre_CheckFACI Extension
 * 20221018   Ludovic TRAVERS                Fix post review issues
 */
 
public class CUS_xt_OIS100_A_Chk_Pre_CheckFACI extends ExtendM3Trigger {
  private final ProgramAPI program
  private final InteractiveAPI interactive
  private final DatabaseAPI database
  private final LoggerAPI logger
  private final MICallerAPI miCaller
  
  String company
  int CONO
  String societe
  String DIVI
  String FACI
  String CUNO
  String SDST
  
  public CUS_xt_OIS100_A_Chk_Pre_CheckFACI(ProgramAPI program, InteractiveAPI interactive,
    DatabaseAPI database, LoggerAPI logger, MICallerAPI miCaller) {
    this.program = program
    this.interactive = interactive
    this.database = database
    this.logger = logger
    this.miCaller = miCaller
  }
  
  public void main() {
    company = program.getLDAZD().CONO
    CONO = (Integer) program.getLDAZD().CONO
    societe = program.getLDAZD().DIVI
    DIVI = ""
    FACI = interactive.display.fields.OAFACI
    CUNO = interactive.display.fields.OACUNO
    SDST = ""
    
    if (FACI != "") {
      DIVI = this.checkByCFACIL(FACI)
      if (DIVI != this.societe) {
        interactive.showCustomError("OAFACI", "Vous n'avez pas le droit d'utiliser l'établissement " + FACI + ", Il n'appartient pas à la société " + this.societe +  " sur laquelle vous êtes connecté !")
      }
    }
    
    if (CUNO != "") {
      SDST = this.callCRS610MI_GetOrderInfo(CUNO)
      if (SDST.equals("890")) {
        // If the CUNO has a SDST = 890, it can only be used for DIVI B40
        // Si le client a un code région 890 alors on ne peut l’utiliser que pour la DIVI B40 
        if (!this.societe.equals("B40")) {
            interactive.showCustomError("OACUNO", "Vous n'avez pas le droit d'utiliser ce client sur la société " + this.societe +  " sur laquelle vous êtes connecté !")
        }
      } else {
        // If the CUNO has a SDST != 890, it can be used for all DIVIs except for DIVI B40.
        // Si le client a un code region différent de 890 alors il peut être utilisé par toutes les DIVI excepté B40
        if (this.societe.equals("B40")) {
          interactive.showCustomError("OACUNO", "Vous n'avez pas le droit d'utiliser ce client sur la société " + this.societe +  " sur laquelle vous êtes connecté !")
        }
      }
    }
  }
  
  /**
  * Access to CFACIL since CRS008MI - Get is not working
  */
  private String checkByCFACIL(String pFACI) {
    DBAction CFACIL = database.table("CFACIL").index("00").selection("CFCONO", "CFFACI", "CFFACN", "CFDIVI", "CFWHLO").build()
    DBContainer container = CFACIL.getContainer()
    container.set("CFCONO", CONO)
    container.set("CFFACI", pFACI)
    if (CFACIL.read(container)) {
      return container.get("CFDIVI")
    } else {
      return ""
    }
  }
    
  /**
  * Call CRS610MI - GetOrderInfo to retrieve SDST
  */
  private String callCRS610MI_GetOrderInfo(String pCUNO) {
    String SDST = ""
    def params = ["CONO" : this.company, "CUNO" : pCUNO]
    
    def callback = {
      Map<String, String> response ->
      if (response.SDST != "") {
        SDST = response.SDST.trim()
      }
    }
    miCaller.call("CRS610MI", "GetOrderInfo", params, callback)
    return SDST
  }
}