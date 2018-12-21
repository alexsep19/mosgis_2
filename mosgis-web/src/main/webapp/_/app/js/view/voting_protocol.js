define ([], function () {

    return function (data, view) {

        $('title').text (data.item.label)
        
        data.item.protocoldate = dt_dmy (data.item.protocoldate)

        fill (view, data.item, $('#body'))

        if (data.item.protocolnum == undefined || data.item.protocolnum == "") {
            $('label[id="num"]').hide ();
        }

        $('#container').w2relayout ({
        
            name: 'topmost_layout',
            
            panels: [
                
                {type: 'main', size: 400,
                
                    tabs: {

                        tabs: [
                            {id: 'voting_protocol_common',   caption: 'Общие'},
                            {id: 'voting_protocol_vote_initiators', caption: 'Инициаторы собрания'},
                            {id: 'voting_protocol_vote_decision_lists', caption: 'Повестка'},
                            {id: 'voting_protocol_docs', caption: 'Файлы'},
                        ].filter (not_off),

                        onClick: $_DO.choose_tab_voting_protocol

                    }                
                
                },
                
            ],
            
            onRender: function (e) {
                clickActiveTab (this.get ('main').tabs, 'voting_protocol.active_tab')
            },

        });

        if (data.item.house_uuid != undefined) {
            $(('#house_link')).attr({title: 'Перейти на страницу паспорта дома'})
            clickOn ($('#house_link'), function () { openTab ('/house/' + data.item.house_uuid) })
        }

    }

})