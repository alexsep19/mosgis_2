define ([], function () {

    return function (data, view) {
        
        $(view).w2uppop ({}, function () {

            $('#w2ui-popup .w2ui-form').w2reform ({

                name: 'mgmt_contract_object_annul_popup_form',

                record: data.record,

                fields : [                
                    {name: 'annulmentinfo', type: 'text'},
                ],
                
            })

       })

    }

})