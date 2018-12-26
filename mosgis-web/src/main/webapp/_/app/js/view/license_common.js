define ([], function () {

    var form_name = 'license_form'
    
    return function (data, view) {
                
        var it = data.item
        
        var orgType = it['org.id_type'];
        
        var orgFormPostfix = orgType === -1 ? 'individual' : 'legal';
        var orgClick = function () {openTab ('/voc_organization_' + orgFormPostfix + '/' + it['org.uuid'])}
        
        $_F5 = function (data) {

            var r = clone (data.item)
            
            var f = w2ui [form_name]                       

            f.record = r
            
            f.refresh ()
        }

        var layout = w2ui ['license_layout']
        
        var $panel = $(layout.el ('main'))
        
        $panel.w2relayout({

            name: 'passport_layout',

            panels: [

                {type: 'top', size: 300},
                {type: 'main', size: 300,

                    tabs: {

                        tabs: [
                            {id: 'license_common_documents', caption: 'Документы лицензионного дела'},
                            {id: 'license_common_houses', caption: 'Информация о домах'},
                            {id: 'license_common_log', caption: 'История'},
                        ],

                        onClick: $_DO.choose_tab_license_common

                    }

                },
            ],

            onRender: function (e) {
                this.get ('main').tabs.click (data.active_tab)
            },

        });
        
        var $panel = $(w2ui ['passport_layout'].el ('top'))
                
        fill (view, data.item, $panel)        

        $panel.w2reform({
            name: 'license_form',
            record: data.item,
            onRefresh: function (e) {e.done (
                function () {
                    clickOn ($('#org'), orgClick)
                }
            )},
        })

        $_F5(data)

    }

})