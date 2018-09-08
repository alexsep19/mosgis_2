define ([], function () {

    return function (data, view) {

        view.attr ({'data-popup-title': data.vc_nsi_3 [data.record.code_vc_nsi_3]}) 

        $(fill (view, data.record)).w2uppop ({}, function () {

            $('#w2ui-popup .w2ui-form').w2reform ({

                name: 'voc_user_form',

                record: data.record,

                fields : [                                
                
                    {name: 'startdate', type: 'date', options: {
                        keyboard: false,
                        start:    dt_dmy (data.item.startdate),
                        end:      dt_dmy (data.item.enddate),
                    }},
                    {name: 'enddate',   type: 'date', options: {
                        keyboard: false,
                        start:    dt_dmy (data.item.startdate),
                        end:      dt_dmy (data.item.enddate),
                    }},
                    {name: 'uuid_contract_agreement', type: 'list', options: {items: data.agreements}},
                ],
                
                focus: -1
                
            })

       })

    }

})