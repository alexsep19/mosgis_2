define ([], function () {

    return function (data, view) {

        $(fill (view, data.record)).w2uppop ({}, function () {

            $('#w2ui-popup .w2ui-form').w2reform ({

                name: 'mgmt_contract_object_new_house_passport_form',

                record: data.record,

                fields : [                                
                    {name: 'type', type: 'radio'},
                ],

            })

       })

    }
    
    $('button[name=create]').click ($_DO.create_mgmt_contract_object_new_house_passport)

})