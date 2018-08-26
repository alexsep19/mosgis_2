define ([], function () {

    $_DO.choose_tab_house_systems = function (e) {
        
        var layout = w2ui ['house_systems_layout']
            
        if (layout) {                
            layout.content ('main', '');
            layout.lock ('main', 'Загрузка...', true);
        }

        var name = e.tab.id

        localStorage.setItem ('house_systems.active_tab', name)

        use.block (e.tab._type)

    }    

    return function (done) {
    
        var house = $('body').data ('data')

        var data = {
        
            systems: [

                {
                    id: 11707,
                    name: 'heating', 
                    label: 'Отопление',
                },
                {
                    id: 11767,
                    name: 'cold_water', 
                    label: 'Холодное водоснабжение'
                },
                {
                    id: 11745,
                    name: 'hot_water', 
                    label: 'Горячее водоснабжение'
                },
                {
                    id: 11789,
                    name: 'sewer', 
                    label: 'Водоотведение'
                },
                {
                    id: 11801,
                    name: 'gas', 
                    label: 'Газ'
                },
                {
                    id: 11665,
                    name: 'electro', 
                    label: 'Электроснабжение'
                },

            ]

        }
        
        data.active_tab = localStorage.getItem ('house_systems.active_tab') || 'house_sys'
        
        $.each (data.systems, function () {
            if (!house.item ['f_' + this.id]) {
                this.off = 1
                if (data.active_tab == this.id) data.active_tab = 'house_sys'
            }
        })        

        done (data)

    }

})