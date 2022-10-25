/**
 * README
 * This extension is being triggered by PPS200/PACHK/PRE
 *
 * Name: CUS_xt_PPS200_A_Chk_Pre_CheckFaciAndWhloCoherence
 * Description: XTend made to forbid the user from choosing a Facility or Warehouse that is not in their DIVI
 * Date       Changed By                     Description
 * 20220601   Ludovic TRAVERS                Create CUS_xt_PPS200_A_Chk_Pre_CheckFaciAndWhloCoherence Extension
 * 20221018   Ludovic TRAVERS                Fix post review issues
 */
 
public class CUS_xt_PPS200_A_Chk_Pre_CheckFaciAndWhloCoherence extends ExtendM3Trigger {
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
  String WHLO
  
  public CUS_xt_PPS200_A_Chk_Pre_CheckFaciAndWhloCoherence(ProgramAPI program, InteractiveAPI interactive,
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
    FACI = interactive.display.fields.WAFACI
    WHLO = interactive.display.fields.WAWHLO
    
    if (FACI != "") {
      DIVI = this.checkByCFACIL(FACI)
      if (DIVI != this.societe) {
        interactive.display.setFocus("WAFACI")
        interactive.showCustomError("WAFACI", "Vous n'avez pas le droit d'utiliser l'établissement " + FACI + ", Il n'appartient pas à la société " + this.societe +  " sur laquelle vous êtes connecté !")
      }
    }
    
    DIVI = ""
    if (WHLO != "") {
      DIVI = this.callMMS005MI_GetWarehouse(WHLO)
      if (DIVI != this.societe) {
        interactive.display.setFocus("WAWHLO")
        interactive.showCustomError("WAWHLO", "Vous n'avez pas le droit d'utiliser le dépôt " + WHLO + ", Il n'appartient pas à la société " + this.societe +  " sur laquelle vous êtes connecté !")
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