define ([], function () {

    return function (data, view) {

        fill (view, data.item, $('#body'))

        $('#container').w2relayout ({
        
            name: 'topmost_layout',
            
            panels: [
                
                {type: 'main', size: 400,
                
                    tabs: {

                        tabs: [
                            {id: 'infrastructure_common',   caption: 'Общие'},
                            {id: 'infrastructure_resources', caption: 'Мощности объекта', off: !data.item.is_object},
                            {id: 'infrastructure_transportation_resources', caption: 'Передача коммунальных ресурсов', off: data.item.is_object},
                            {id: 'infrastructure_docs', caption: 'Документы'},
                            {id: 'infrastructure_common_log', caption: 'История изменений'},
                        ].filter (not_off),

                        onClick: $_DO.choose_tab_infrastructure

                    }                
                
                },
                
            ],
            
            onRender: function (e) {
                clickActiveTab (this.get ('main').tabs, 'infrastructure.active_tab')
            },

        });

    }

})