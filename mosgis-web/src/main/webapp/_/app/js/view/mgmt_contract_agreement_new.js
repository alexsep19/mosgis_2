define ([], function () {

    return function (data, view) {

        var contract = data.contract       

        $(view).w2popup('open', {

            width  : 605,
            height : 250,

            title   : 'Новое дополнительное соглашение',

            onOpen: function (event) {

                event.onComplete = function () {

                    var name = 'mgmt_contract_agreement_new_form'

                    if (w2ui [name]) w2ui [name].destroy ()

                    $('#w2ui-popup .w2ui-form').w2form ({

                        name: name,

                        fields : [
                            {name: 'description',  type: 'textarea' },
                            {name: 'agreementdate',  type: 'date' },
                            {name: 'agreementnumber',  type: 'text' },
                            {name: 'files', type: 'file', options: {max: 1}},
                        ],                        

                    });

                    clickOn ($('#w2ui-popup button'), $_DO.update_mgmt_contract_agreement_new)

                }

            }

        });

    }

});