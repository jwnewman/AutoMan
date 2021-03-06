package edu.umass.cs.automan.core

import answer._
import edu.umass.cs.automan.core.question._
import java.util.Locale
import java.text.NumberFormat
import actors.Future
import memoizer.{ThunkLogger, AutomanMemoizer}
import scheduler.Thunk
import strategy._
import edu.umass.cs.automan.adapters.MTurk.question.MTQuestionOption

abstract class AutomanAdapter[RBQ <: RadioButtonQuestion[MTQuestionOption],
                              CBQ <: CheckboxQuestion[MTQuestionOption],
                              FTQ <: FreeTextQuestion] {
  protected var _budget: BigDecimal = 0.00
  protected var _confidence: Double = 0.95
  protected var _locale: Locale = Locale.getDefault
//  protected var _max_replicas = 30
  protected var _strategy: Class[_ <: ValidationStrategy] = classOf[DefaultStrategy]
  protected var _memoizer: AutomanMemoizer = _
  protected var _memo_conn_string: String = "jdbc:derby:AutomanMemoDB;create=true"
  protected var _memo_user: String = ""
  protected var _memo_pass: String = ""
  protected var _thunklog: ThunkLogger = _
  protected var _thunk_conn_string: String = "jdbc:derby:ThunkLogDB;create=true"
  protected var _thunk_user: String = ""
  protected var _thunk_pass: String = ""

  // getters and setters
  def accept(t: Thunk)
  def budget: BigDecimal = _budget
  def budget_=(b: BigDecimal) { _budget = b }
  def cancel(t: Thunk)
  def confidence: Double = _confidence
  def confidence_=(c: Double) { _confidence = c }
  def memo_init() {
    _memoizer = new AutomanMemoizer(_memo_conn_string, _memo_user, _memo_pass)
  }
  def thunklog_init() {
    _thunklog = new ThunkLogger(_thunk_conn_string, _thunk_user, _thunk_pass)
  }
  def post(ts: List[Thunk], dual: Boolean, exclude_worker_ids: List[String])
  def process_custom_info(t: Thunk, i: Option[String])
  def reject(t: Thunk)
  def retrieve(ts: List[Thunk]) : List[Thunk]  // returns all thunks passed in
  def strategy = _strategy
  def strategy_=(s: Class[ValidationStrategy]) { _strategy = s }

  // Question creation
  def CheckboxQuestion(fq: CBQ => Unit) : Future[CheckboxAnswer]
  def FreeTextQuestion(fq: FTQ => Unit) : Future[FreeTextAnswer]
  def RadioButtonQuestion(fq: RBQ => Unit) : Future[RadioButtonAnswer]
  
  // Option creation
  def Option(id: Symbol, text: String) : QuestionOption

  // Global backend config
  def budget_formatted = {
    val dbudget = _budget.setScale(2, BigDecimal.RoundingMode.HALF_EVEN)
    val nf = NumberFormat.getCurrencyInstance(_locale)
    nf.setMinimumFractionDigits(1)
    nf.setMaximumFractionDigits(2)
    nf.format(dbudget.doubleValue())
  }
  def get_budget_from_backend(): BigDecimal
  def locale: Locale = _locale
  def locale_=(l: Locale) { _locale = l }
  def schedule(q: RBQ): Future[RadioButtonAnswer]
  def schedule(q: CBQ): Future[CheckboxAnswer]
}