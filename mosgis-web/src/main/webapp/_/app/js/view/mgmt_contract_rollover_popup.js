define ([], function () {

    return function (data, view) {
        
        $(view).w2uppop ({}, function () {

            $('#w2ui-popup .w2ui-form').w2reform ({

                name: 'mgmt_contract_rollover_popup_form',

                record: data.record,

                fields : [                
                    {name: 'rolltodate', type: 'date'},
                ],
                
            })

       })

    }

})