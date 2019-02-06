define ([], function () {

    return function (data, view) {

        $(fill (view, data.record)).w2uppop ({}, function () {

            $('#w2ui-popup .w2ui-form').w2reform ({

                name: 'account_ind_new_form',

                record: data.record,

                fields : [  
                
                    {name: 'accountnumber', type: 'text'},
                    
                    {name: 'isaccountsdivided', type: 'list', options: {items: [
                        {id: -1, text: '[нет данных]'},
                        {id:  0, text: 'нет, не разделен(ы)'},
                        {id:  1, text: 'да, разделен(ы)'},
                    ]}},
                    
                    {name: 'isrenter', type: 'list', options: {items: [
                        {id: -1, text: '[нет данных]'},
                        {id:  0, text: 'нет, не является нанимателем'},
                        {id:  1, text: 'да, является нанимателем'},
                    ]}},
                    
                    {name: 'totalsquare', type: 'float', options: {min: 0, precision: 2}},
                    
                    {name: 'uuid_person', type: 'list', options: 
                        {
                            url: '/mosgis/_rest/?type=vc_persons',
                            postData: {
                                uuid_org: $_USER.uuid_org, 
                                searchLogic: 'OR',
                                offset: 0,
                                limit: 50,
                            },
                            cacheMax: 50,
                            filter: false,

                            onSearch: function (e) {
                                this.options.postData['search'] = [{'value': e.search}]
                            },

                            onLoad: function (e) {
                                dia2w2ui (e)
                                e.xhr.responseJSON = JSON.parse (e.xhr.responseText)
                                e.data = e.xhr.responseJSON                                
                                $.each (e.data.records, function () {this.text = this.label})
                            }
                            
                        }
                    },                    

                ],

            })

       })

    }

})