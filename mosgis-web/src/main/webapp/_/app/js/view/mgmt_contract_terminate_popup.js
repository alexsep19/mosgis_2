define ([], function () {

    return function (data, view) {
    
        var name = 'mgmt_contract_terminate_popup_form'
        
        $(view).w2popup('open', {

            width  : 580,
            height : 240,

            title   : 'Расторжение договора управления',

            onOpen: function (event) {

                event.onComplete = function () {

                    if (w2ui [name]) w2ui [name].destroy ()

                    $('#w2ui-popup .w2ui-form').w2form ({

                        name: name,

                        fields : [
                            {name: 'code_vc_nsi_54',  type: 'list', options: {items: data.vc_nsi_54.items}},
                            {name: 'terminate',  type: 'date' },
                            {name: 'files', type: 'file', options: {max: 1}},
                        ],
                                                
                    });

                    clickOn ($('#w2ui-popup button'), $_DO.update_mgmt_contract_terminate_popup)

                }

            }

        });

    }

});