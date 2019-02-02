import * as $ from 'jquery';
import {CompanyTransactions} from '../entities/companyTransactions';
import {Transaction} from '../entities/transaction';
import {Authenticator} from '../plumbing/oauth/authenticator';
import {HttpClient} from '../plumbing/utilities/httpClient';

/*
 * Logic related to the transactions view
 */
export class TransactionsView {

    /*
     * Fields
     */
    private _authenticator: Authenticator;
    private _apiBaseUrl: string;
    private _companyId: string;

    /*
     * Class setup
     */
    public constructor(authenticator: Authenticator, apiBaseUrl: string, companyId: string) {
        this._authenticator = authenticator;
        this._apiBaseUrl = apiBaseUrl;
        this._companyId = companyId;
        this._setupCallbacks();
    }

    /*
     * Wait for data then render it
     */
    public async execute(): Promise<void> {

        try {
            const url = `${this._apiBaseUrl}/companies/${this._companyId}/transactions`;
            const data = await HttpClient.callApi(url, 'GET', null, this._authenticator) as CompanyTransactions;
            return this._renderData(data);

        } catch (uiError) {

            // If an invalid or unauthorized id is typed in the browser then return to the list page
            if (uiError.statusCode === 404) {
                location.hash = '#';
            } else {
                throw uiError;
            }
        }
    }

    /*
     * Hide UI elements when the view unloads
     */
    public unload(): void {
        $('.transactionscontainer').addClass('hide');
    }

    /*
     * Render data after receiving it from the API
     */
    private _renderData(data: CompanyTransactions): void {

        // Show and clear
        $('.transactionscontainer').removeClass('hide');
        $('.transactionslist').html('');
        $('.transactionsheader').text(`Transactions for ${data.company.name}`);

        data.transactions.forEach((transaction: Transaction) => {

          // Format fields for display
          const formattedAmountUsd = Number(transaction.amountUsd).toLocaleString();

          // Render the UI
          const transactionDiv = $(`<div class='item col-md-3 col-xs-6'>
                                      <div class='thumbnail'>
                                        <div class='caption row'>
                                          <div class='col-xs-6 text-left'>Transaction Id</div>
                                          <div class='col-xs-6 text-right link'>${transaction.id}</div>
                                        </div>
                                        <div class='caption row'>
                                          <div class='col-xs-6 text-left'>Investor Id</div>
                                          <div class='col-xs-6 text-right link'>${transaction.investorId}</div>
                                        </div>
                                        <div class='caption row'>
                                          <div class='col-xs-6 text-left'>Amount USD</div>
                                          <div class='col-xs-6 text-right amount'>${formattedAmountUsd}</div>
                                        </div>
                                      </div>
                                    </div>`);

          // Update the DOM
          $('.transactionslist').append(transactionDiv);
      });
    }

    /*
     * Plumbing to ensure that the this parameter is available in async callbacks
     */
    private _setupCallbacks(): void {
        this._renderData = this._renderData.bind(this);
   }
}
