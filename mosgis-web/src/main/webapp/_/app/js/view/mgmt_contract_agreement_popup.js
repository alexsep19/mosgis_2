define ([], function () {

    return function (data, view) {

        $(view).w2popup ('open', {

            width  : 625,
            height : 250,

            title   : 'Дополнительное соглашение',

            onOpen: function (event) {

                event.onComplete = function () {
                                
                    $('#file_label').text (data.label)                    

                    var name = 'mgmt_contract_agreement_popup_form'

                    if (w2ui [name]) w2ui [name].destroy ()

                    $('#w2ui-popup .w2ui-form').w2form ({

                        name: name,

                        record: data,

                        fields : [
                            {name: 'description',  type: 'textarea' },
                            {name: 'agreementdate',  type: 'date' },
                            {name: 'agreementnumber',  type: 'text' },
                        ],

                        focus: -1

                    });

                    clickOn ($('#w2ui-popup button'), $_DO.update_mgmt_contract_agreement_popup)
                    clickOn ($('#file_label'), $_DO.download_mgmt_contract_agreement_popup)
                    
                }

            }

        })

    }    

});