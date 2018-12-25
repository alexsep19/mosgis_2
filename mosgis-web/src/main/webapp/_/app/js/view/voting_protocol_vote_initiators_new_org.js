define ([], function () {

    return function (data, view) {
        
        var name = 'voting_protocol_vote_initiators_new_org_form'

        var grid = w2ui ['voting_protocol_vote_initiators_grid']

        $(view).w2popup('open', {

            width  : 500,
            height : 150,

            title   : 'Добавление инициатора - организации',

            onOpen: function (event) {

                event.onComplete = function () {

                    var name = 'voting_protocol_vote_initiators_new_org_form'

                    if (w2ui [name]) w2ui [name].destroy ()

                    $('#w2ui-popup .w2ui-form').w2form ({

                        name: name,

                        fields : [
                            {name: 'uuid_org', type: 'list', options: 
                                {
                                    url: '/mosgis/_rest/?type=voc_organizations&part=list',
                                    postData: {'protocol_uuid': data.item.uuid, 'searchLogic': 'OR'},
                                    cacheMax: 10,
                                    filter: false,

                                    onSearch: function (e) {

                                        this.options.postData['search'] = [{'value': e.search}]

                                    },

                                    onLoad: function (e) {

                                        dia2w2ui (e)
                                        e.xhr.responseJSON = JSON.parse (e.xhr.responseText)
                                        e.data = e.xhr.responseJSON
                                        
                                    }
                                }
                            },
                        ],

                    });

                    clickOn ($('#w2ui-popup button'), $_DO.update_voting_protocol_vote_initiators_new_org)

                }
                
            }
            
        });
    
    }
    
    
});