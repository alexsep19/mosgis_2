define ([], function () {

    return function (data, view) {
    
        var name = 'supply_resource_contract_terminate_popup_form'
        
        var it = data.item
        
        $(view).w2popup('open', {

            width  : 545,
            height : 200,

            title   : 'Прекращение действия договора ресурсоснабжения',

            onOpen: function (event) {

                event.onComplete = function () {

                    if (w2ui [name]) w2ui [name].destroy ()

                    $('#w2ui-popup .w2ui-form').w2form ({

                        name: name,
                        
                        record: {
                            terminate: it.terminate || dt_dmy(new Date().toISOString()),
                            code_vc_nsi_54: it.code_vc_nsi_54
                        },

                        fields : [
                            {name: 'code_vc_nsi_54',  type: 'list', options: {items: data.vc_nsi_54.items}},
                            {name: 'terminate', type: 'date'},
                        ],

                        focus: 1,
                    });

                    clickOn ($('#w2ui-popup button'), $_DO.update_supply_resource_contract_terminate_popup)

                }

            }

        });

    }

});