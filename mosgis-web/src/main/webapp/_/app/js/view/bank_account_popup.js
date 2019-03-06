define ([], function () {

    return function (data, view) {    
        
        var now = dt_dmy (new Date ().toJSON ())

        $(fill (view, data.record)).w2uppop ({}, function () {

            $('#w2ui-popup .w2ui-form').w2reform ({

                name: 'bank_account_popup_form',

                record: data.record,

                fields : [
                    {name: 'accountnumber', type: 'text'},
                    {name: 'closedate', type: 'date', options: {end: now}},
                    {name: 'opendate', type: 'date', options: {end: now}},
                    {name: 'bikcredorg', type: 'list', hint: 'Адрес', options: {
                        
                        url: '/mosgis/_rest/?type=voc_bic',
                        
                        filter: false,
                        
                        cacheMax: 50,
                        
                        postData: {offset: 0, limit: 50},
                        
                        onSearch: function (e) {
                            this.options.postData.search = [{'value': e.search}]
                            this.options.postData.searchLogic = "OR"
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

            })

       })

    }

})