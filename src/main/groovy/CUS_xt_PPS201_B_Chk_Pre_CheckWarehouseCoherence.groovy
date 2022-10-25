/**
 * README
 * This extension is being triggered by PPS201/PBCHK/PRE
 *
 * Name: CUS_xt_PPS201_B_Chk_Pre_CheckWarehouseCoherence
 * Description: XTend made to forbid the user from choosing a Warehouse that is not in their DIVI
 * Date       Changed By                     Description
 * 20220601   Ludovic TRAVERS                Create CUS_xt_PPS201_B_Chk_Pre_CheckWarehouseCoherence Extension
 * 20221018   Ludovic TRAVERS                Fix post review issues
 */

public class CUS_xt_PPS201_B_Chk_Pre_CheckWarehouseCoherence extends ExtendM3Trigger {
  private final ProgramAPI program
  private final InteractiveAPI interactive
  private final DatabaseAPI database
  private final LoggerAPI logger
  private final MICallerAPI miCaller
  
  String company
  int CONO
  String societe
  String DIVI
  String WHLO
  
  public CUS_xt_PPS201_B_Chk_Pre_CheckWarehouseCoherence(ProgramAPI program, InteractiveAPI interactive,
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
    WHLO = interactive.display.fields.WBWHLO
    DIVI = this.callMMS005MI_GetWarehouse(WHLO)
    if (DIVI != this.societe) {
      interactive.showCustomError("WBWHLO", "Vous n'avez pas le droit d'utiliser le dépôt " + WHLO + ", Il n'appartient pas à la société " + this.societe +  " sur laquelle vous êtes connecté !")
    }
  }
  
  /**
  * Call MMS005MI - GetWarehouse to retrieve DIVI related to Warehouse
  */
  private String callMMS005MI_GetWarehouse(String pWHLO) {
    String oDIVI = ""
    def params = ["WHLO" : pWHLO]
    def callback = {
      Map<String, String> response ->
      if (response.DIVI != null) {
        oDIVI = response.DIVI.trim()
      }
    }
    miCaller.call("MMS005MI", "GetWarehouse", params, callback)
    return oDIVI
  }
}
