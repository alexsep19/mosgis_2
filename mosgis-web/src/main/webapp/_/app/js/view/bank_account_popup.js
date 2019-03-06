define ([], function () {

    return function (data, view) {    
        
        var now = dt_dmy (new Date ().toJSON ())        

        $(fill (view, data.record)).w2uppop ({}, function () {

            $('#w2ui-popup .w2ui-form').w2reform ({

                name: 'bank_account_popup_form',

                record: data.record,

                fields : [
                    {name: 'accountnumber', type: 'text'},
//                    {name: 'bikcredorg', type: 'text'},
                    {name: 'closedate', type: 'date', options: {end: now}},
                    {name: 'opendate', type: 'date', options: {end: now}},
                    {name: 'bikcredorg', type: 'list', hint: 'Адрес', options: {
                        
                        url: '/mosgis/_rest/?type=voc_bic',
                        
                        filter: false,
                        
                        cacheMax: 50,
                        
                        postData: {offset: 0, limit: 50},
                        
                        onSearch: function (e) {
                            this.options.postData['search'] = [{'value': e.search}]
                        },
                        
                        onLoad: function (e) {
                            e.data = {
                                status: "success",
                                records: e.data.content.root.map(function (i) {
                                    return {
                                        id: i.id,
                                        text: i.bic + ' ' + i.namep
                                    }
                                })
                            }
                        }
                        
                    }},
                    
                ],
/*                
                onChange: function (e) {                
                    if (e.target == 'fiashouseguid') e.done ($_DO.load_premises_for_bank_account_popup)
                },
                
                onRender: function (e) {                
                    if (!is_virgin) return
                    is_virgin = false                
                    if (data.record.fiashouseguid) e.done ($_DO.load_premises_for_bank_account_popup)
                }
*/
            })

       })

    }

})